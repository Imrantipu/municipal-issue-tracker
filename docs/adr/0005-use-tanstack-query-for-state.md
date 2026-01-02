# 5. Use TanStack Query for Server State Management

Date: 2025-12-26

## Status

Accepted

## Context

We need to manage state in our Next.js frontend application.

**Types of State:**
1. **Server State**: Data from Spring Boot API (issues, users, timeline)
2. **UI State**: Modal open/close, form inputs, dropdown selections
3. **Auth State**: Current user info, JWT token

**Requirements:**
- Fetch issues from API and display in tables
- Real-time data synchronization (when staff updates issue, citizen sees it)
- Optimistic updates (instant UI feedback, sync with server later)
- Loading states, error handling, retry logic
- Cache API responses (reduce redundant network requests)

**Options Considered:**
1. **useState + useEffect** (manual fetching, basic React)
2. **Context API** (global state, but manual data fetching)
3. **Redux Toolkit** (complex state management, includes RTK Query)
4. **Zustand** (lightweight global state, but manual API logic)
5. **TanStack Query (React Query v5)** (specialized for server state)

## Decision

We will use **TanStack Query v5** (formerly React Query) for ALL server state management.

**For other state:**
- **UI State**: Local `useState` (no global store needed)
- **Auth State**: TanStack Query + localStorage (user info cached)

**Explicitly NOT using:**
- ❌ Redux Toolkit (too complex for our needs)
- ❌ Zustand (redundant if we have TanStack Query)
- ❌ Context API for server data (manual work that TanStack Query does automatically)

### Implementation:
- **Library**: `@tanstack/react-query` v5
- **DevTools**: `@tanstack/react-query-devtools` (development only)
- **Cache Time**: 5 minutes (stale data refetched after 5 min)
- **Retry Logic**: 3 retries with exponential backoff
- **Refetch**: On window focus (ensure fresh data when user returns to tab)

## Consequences

### Positive ✅

1. **Automatic Caching**
   - Fetches data once, caches it
   - Subsequent requests use cached data (no redundant API calls)
   - Reduces server load and improves performance
   - Example: Navigate away from issues page and back = instant load from cache

2. **Loading & Error States (Built-in)**
   ```tsx
   const { data, isLoading, isError, error } = useQuery({
     queryKey: ['issues'],
     queryFn: fetchIssues,
   });

   if (isLoading) return <Spinner />;
   if (isError) return <ErrorMessage error={error} />;
   return <IssueList issues={data} />;
   ```
   No manual `isLoading` state management needed!

3. **Automatic Refetching**
   - Refetch on window focus (user switches back to tab)
   - Refetch on reconnect (user regains internet connection)
   - Polling (refetch every X seconds for real-time data)
   - Ensures data is always fresh

4. **Optimistic Updates**
   - Update UI immediately, sync with server in background
   - Rollback if server request fails
   - Example: Citizen creates issue → shows in list instantly → syncs with API
   ```tsx
   const mutation = useMutation({
     mutationFn: createIssue,
     onMutate: async (newIssue) => {
       // Cancel ongoing fetches
       await queryClient.cancelQueries({ queryKey: ['issues'] });

       // Snapshot previous value
       const previousIssues = queryClient.getQueryData(['issues']);

       // Optimistically update cache
       queryClient.setQueryData(['issues'], (old) => [...old, newIssue]);

       return { previousIssues }; // Return context for rollback
     },
     onError: (err, newIssue, context) => {
       // Rollback on error
       queryClient.setQueryData(['issues'], context.previousIssues);
     },
     onSettled: () => {
       // Refetch to ensure sync
       queryClient.invalidateQueries({ queryKey: ['issues'] });
     },
   });
   ```

5. **Pagination & Infinite Scroll**
   - Built-in support with `useInfiniteQuery`
   - Automatically manages page state and data merging
   - Perfect for issue lists (load more on scroll)

6. **Request Deduplication**
   - Multiple components request same data = only 1 API call
   - Example: Dashboard shows issue count AND issue list = 1 fetch

7. **DevTools**
   - Visual query inspector (see cache, loading states, errors)
   - Debug data fetching issues easily
   - Shows query timeline and network requests

8. **TypeScript Support**
   - Full type inference (data type known automatically)
   - Type-safe query keys (prevent typos)
   - Generic hooks: `useQuery<Issue[]>`

9. **German Market Appeal**
   - TanStack Query is VERY popular in modern React apps
   - Shows understanding of state management beyond Redux
   - Demonstrates knowledge of specialized tools (not just general-purpose)
   - Common in Next.js + Spring Boot stacks

10. **No Boilerplate**
    - Compared to Redux: No actions, reducers, dispatch, selectors
    - Just: `useQuery` (read) and `useMutation` (write)
    - 90% less code for same functionality

### Negative ❌

1. **Learning Curve**
   - Need to understand caching strategies (staleTime, cacheTime)
   - Query invalidation patterns (when to refetch)
   - Optimistic updates are tricky at first
   - **Mitigation**: Excellent documentation, common patterns in our app

2. **Cache Synchronization**
   - Multiple related queries need careful invalidation
   - Example: Update issue → invalidate both "issues" and "issue detail" cache
   - **Mitigation**: Clear naming convention for query keys

3. **Not for Global UI State**
   - TanStack Query is for SERVER state only
   - Cannot store UI state (modal open, theme, etc.)
   - Need useState or Zustand for pure client state
   - **Mitigation**: For our app, most state IS server state; minimal UI state needed

4. **Debugging Cache Issues**
   - Sometimes data doesn't refetch when expected (cache still valid)
   - Need to understand staleTime vs cacheTime
   - **Mitigation**: Use DevTools, clear documentation in code

### Neutral 🔄

1. **Bundle Size**
   - ~13KB gzipped (small but not zero)
   - Worth it for the features provided

2. **SSR/SSG with Next.js**
   - TanStack Query supports Next.js App Router
   - Need `QueryClientProvider` wrapper
   - Prefetching in Server Components is possible but optional

## Why TanStack Query Fits Our Project

### 1. Our Data Fetching Patterns

We have LOTS of server data:
- Issues (list, filter, paginate)
- Issue details (single issue + timeline)
- User info (current user, staff list for assignment)
- Dashboard statistics (count by status, category)

TanStack Query handles all of this elegantly:

```tsx
// Fetch issues list
const { data: issues } = useQuery({
  queryKey: ['issues'],
  queryFn: () => apiClient<Issue[]>('/issues'),
});

// Fetch single issue (cached separately)
const { data: issue } = useQuery({
  queryKey: ['issue', issueId],
  queryFn: () => apiClient<Issue>(`/issues/${issueId}`),
});

// Fetch current user
const { data: user } = useQuery({
  queryKey: ['user', 'me'],
  queryFn: () => apiClient<User>('/auth/me'),
  staleTime: Infinity, // User info rarely changes
});
```

### 2. Mutations (Create, Update, Delete)

```tsx
// Create issue
const createIssueMutation = useMutation({
  mutationFn: (newIssue: CreateIssueDto) =>
    apiClient('/issues', {
      method: 'POST',
      body: JSON.stringify(newIssue),
    }),
  onSuccess: () => {
    // Invalidate and refetch issues list
    queryClient.invalidateQueries({ queryKey: ['issues'] });
    toast.success('Issue created successfully!');
    router.push('/citizen/issues');
  },
  onError: (error) => {
    toast.error('Failed to create issue');
  },
});

// Usage in component
const handleSubmit = (data: CreateIssueDto) => {
  createIssueMutation.mutate(data);
};
```

### 3. Role-Based Data Fetching

Different roles see different data:

```tsx
// Admin: All issues
const { data: allIssues } = useQuery({
  queryKey: ['issues', 'all'],
  queryFn: () => apiClient<Issue[]>('/admin/issues'),
  enabled: user?.role === 'ADMIN', // Only fetch if admin
});

// Staff: Assigned issues
const { data: assignedIssues } = useQuery({
  queryKey: ['issues', 'assigned', user?.id],
  queryFn: () => apiClient<Issue[]>('/staff/assigned-issues'),
  enabled: user?.role === 'STAFF',
});

// Citizen: My issues
const { data: myIssues } = useQuery({
  queryKey: ['issues', 'my', user?.id],
  queryFn: () => apiClient<Issue[]>('/citizen/my-issues'),
  enabled: user?.role === 'CITIZEN',
});
```

### 4. Real-Time Data (Polling)

For staff dashboard (see new issues in real-time):

```tsx
const { data: pendingIssues } = useQuery({
  queryKey: ['issues', 'pending'],
  queryFn: () => apiClient<Issue[]>('/issues?status=PENDING'),
  refetchInterval: 30000, // Refetch every 30 seconds
});
```

### 5. Dependent Queries

Fetch user first, then fetch their issues:

```tsx
// 1. Fetch user
const { data: user } = useQuery({
  queryKey: ['user', 'me'],
  queryFn: () => apiClient<User>('/auth/me'),
});

// 2. Fetch user's issues (only after user is loaded)
const { data: issues } = useQuery({
  queryKey: ['issues', user?.id],
  queryFn: () => apiClient<Issue[]>(`/users/${user.id}/issues`),
  enabled: !!user, // Only run if user exists
});
```

## Setup

### 1. Install
```bash
npm install @tanstack/react-query @tanstack/react-query-devtools
```

### 2. Create QueryClient Provider
```tsx
// app/providers.tsx
"use client";

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { useState } from 'react';

export function Providers({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(() => new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 5 * 60 * 1000, // 5 minutes
        cacheTime: 10 * 60 * 1000, // 10 minutes
        retry: 3,
        retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
        refetchOnWindowFocus: true,
        refetchOnReconnect: true,
      },
    },
  }));

  return (
    <QueryClientProvider client={queryClient}>
      {children}
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
}
```

### 3. Wrap App
```tsx
// app/layout.tsx
import { Providers } from './providers';

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>
        <Providers>
          {children}
        </Providers>
      </body>
    </html>
  );
}
```

## Comparison: TanStack Query vs Alternatives

| Feature | TanStack Query | Redux Toolkit + RTK Query | Zustand + Manual Fetch |
|---------|----------------|---------------------------|------------------------|
| **Caching** | ✅ Automatic | ✅ Automatic | ❌ Manual |
| **Loading States** | ✅ Built-in | ✅ Built-in | ❌ Manual |
| **Refetching** | ✅ Automatic | ✅ Automatic | ❌ Manual |
| **Optimistic Updates** | ✅ Built-in | ✅ Built-in | ❌ Manual |
| **DevTools** | ✅ Excellent | ✅ Good | 🟡 Basic |
| **Boilerplate** | ✅ Minimal | 🟡 Moderate | ✅ Minimal |
| **Learning Curve** | 🟡 Moderate | 🔴 Steep | ✅ Easy |
| **Bundle Size** | ✅ 13KB | 🟡 40KB (Redux + RTK) | ✅ 3KB |
| **TypeScript** | ✅ Excellent | ✅ Excellent | ✅ Good |
| **Server State** | ✅ Perfect | ✅ Good | 🟡 Manual work |
| **UI State** | ❌ Not designed for it | ✅ Good | ✅ Perfect |
| **Popularity (2024+)** | ✅ Growing fast | 🟡 Declining | ✅ Rising |

**Verdict**: TanStack Query is the best choice for server state (our primary need).

## Query Key Strategy

Consistent naming prevents bugs:

```typescript
// lib/queryKeys.ts
export const queryKeys = {
  // User queries
  user: {
    me: ['user', 'me'] as const,
    byId: (id: number) => ['user', id] as const,
    all: ['users'] as const,
  },

  // Issue queries
  issues: {
    all: ['issues'] as const,
    byStatus: (status: string) => ['issues', { status }] as const,
    byId: (id: number) => ['issue', id] as const,
    my: (userId: number) => ['issues', 'my', userId] as const,
    assigned: (staffId: number) => ['issues', 'assigned', staffId] as const,
  },

  // Timeline queries
  timeline: {
    byIssue: (issueId: number) => ['timeline', issueId] as const,
  },
};

// Usage
const { data } = useQuery({
  queryKey: queryKeys.issues.byStatus('PENDING'),
  queryFn: () => apiClient('/issues?status=PENDING'),
});
```

## Testing

TanStack Query is very testable:

```tsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import { IssueList } from './IssueList';

test('displays issues', async () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  render(
    <QueryClientProvider client={queryClient}>
      <IssueList />
    </QueryClientProvider>
  );

  await waitFor(() => {
    expect(screen.getByText('Road pothole')).toBeInTheDocument();
  });
});
```

## Implementation Checklist

Sprint 1 (Week 1):
- [ ] Install TanStack Query + DevTools
- [ ] Create `QueryClientProvider` wrapper
- [ ] Define query key naming convention
- [ ] Implement `useQuery` for fetching issues
- [ ] Implement `useMutation` for creating issues
- [ ] Add loading and error UI components
- [ ] Test caching behavior

Sprint 2-3:
- [ ] Add optimistic updates for mutations
- [ ] Implement pagination for issue lists
- [ ] Add real-time polling for staff dashboard
- [ ] Set up query invalidation patterns
- [ ] Write tests for custom hooks

## References

- [TanStack Query Documentation](https://tanstack.com/query/latest)
- [TanStack Query with Next.js App Router](https://tanstack.com/query/latest/docs/react/guides/advanced-ssr)
- [Practical React Query by Dominik](https://tkdodo.eu/blog/practical-react-query)
- [Query Key Factories](https://tkdodo.eu/blog/effective-react-query-keys)
