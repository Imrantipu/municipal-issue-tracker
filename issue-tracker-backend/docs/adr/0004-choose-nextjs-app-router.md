# 4. Choose Next.js 14+ with App Router

Date: 2025-12-26

## Status

Accepted

## Context

We need to choose a frontend framework for our Public Infrastructure Issue Reporting System.

**Requirements:**
- 3 different dashboards (Admin, Staff, Citizen)
- Forms: Registration, Login, Create Issue, Update Issue
- Data tables: Issue lists with filtering, sorting
- Image upload for issue reports
- Responsive design (mobile-friendly)
- Good SEO (public pages should be discoverable)

**Developer Background:**
- Strong React.js experience from MERN course
- Experience with Next.js (modern full-stack framework)
- Familiar with TypeScript
- Wants to demonstrate mid-level skills for German job market

**Options Considered:**
1. **Create React App (CRA)** - Basic React, client-side only
2. **Vite + React** - Fast dev server, modern build tool
3. **Next.js 14+ App Router** - Full-stack React framework
4. **Next.js Pages Router** - Older Next.js routing system

## Decision

We will use **Next.js 14+ with App Router** (NOT Pages Router).

### Tech Stack:
- **Framework**: Next.js 14+ (App Router)
- **Language**: TypeScript (strict mode)
- **UI Library**: Shadcn UI + Radix UI primitives
- **Styling**: Tailwind CSS 3
- **Forms**: React Hook Form + Zod validation
- **API Calls**: TanStack Query v5 (see ADR 0005)
- **Image Upload**: Next.js Image component + ImageBB (free tier)

### Project Structure:
```
src/
├── app/                    # App Router (Next.js 14+)
│   ├── (auth)/            # Route group (auth pages)
│   │   ├── login/
│   │   └── register/
│   ├── (dashboard)/       # Route group (protected pages)
│   │   ├── admin/
│   │   ├── staff/
│   │   └── citizen/
│   ├── layout.tsx         # Root layout
│   ├── page.tsx           # Home page
│   └── api/               # API routes (NOT used for backend - we use Spring Boot)
│
├── components/            # React components
│   ├── ui/               # Shadcn UI components
│   ├── forms/            # Form components
│   ├── layouts/          # Layout components
│   └── features/         # Feature-specific components
│
├── lib/                  # Utilities
│   ├── api.ts           # API client (fetch wrapper)
│   ├── auth.ts          # Auth utilities
│   └── utils.ts         # Helper functions
│
└── types/               # TypeScript types
```

## Consequences

### Positive ✅

1. **Server Components (New in App Router)**
   - Components render on server by default (faster initial load)
   - Reduces JavaScript sent to browser (better performance)
   - Can fetch data directly in components (no useEffect for initial data)
   - Example:
     ```tsx
     // Server Component (default in App Router)
     export default async function IssuesPage() {
       const issues = await fetchIssues(); // Direct data fetch
       return <IssueList issues={issues} />;
     }
     ```

2. **Built-in Routing**
   - File-based routing (no react-router-dom config)
   - Nested layouts (admin layout wraps all admin pages)
   - Route groups `(auth)` and `(dashboard)` for organization
   - Loading states (`loading.tsx`) and error boundaries (`error.tsx`) per route

3. **SEO-Friendly**
   - Server-side rendering (SSR) by default
   - Search engines can crawl content easily
   - Dynamic metadata per page:
     ```tsx
     export const metadata = {
       title: 'My Issues - Issue Tracker',
       description: 'View and manage your reported issues',
     };
     ```

4. **TypeScript Support**
   - First-class TypeScript support (no config needed)
   - Auto-completion for routes (type-safe navigation)
   - Strict type checking prevents bugs

5. **Image Optimization**
   - `next/image` component (automatic lazy loading, WebP conversion)
   - Responsive images (srcset generated automatically)
   - Blur placeholders for better UX

6. **Developer Experience**
   - Fast Refresh (instant feedback on code changes)
   - Excellent error messages
   - Built-in linting (ESLint config for Next.js)
   - Vercel deployment (one-click deployment for MVP)

7. **German Market Appeal**
   - Next.js is VERY popular in German startups and companies
   - Shows understanding of modern React patterns
   - Server components = cutting-edge knowledge
   - TypeScript = professional development standard

8. **Full-Stack Capabilities (If Needed)**
   - Can add API routes later (for image proxy, webhooks, etc.)
   - Server actions for mutations (new pattern, very powerful)
   - Currently using Spring Boot backend, but flexibility is nice

9. **Performance**
   - Automatic code splitting (each page loads only needed JS)
   - Route prefetching (links prefetch on hover)
   - Streaming SSR with Suspense boundaries
   - React Server Components reduce client bundle size

### Negative ❌

1. **Learning Curve (App Router)**
   - App Router is newer (released 2023, stable in Next.js 13.4+)
   - Different mental model from Pages Router
   - Need to understand Server vs Client Components
   - "use client" directive for client components (interactive)
   - **Mitigation**: Developer already knows React; App Router is the future; worth learning

2. **Server Components Limitations**
   - Cannot use useState, useEffect, browser APIs in Server Components
   - Must add "use client" for interactive components
   - Can be confusing at first (which component should be client vs server?)
   - **Mitigation**: Clear rules: interactive = client, static = server

3. **Caching Complexity**
   - Next.js aggressive caching can cause confusion (data not updating)
   - Need to understand caching strategies (force-cache, no-store, revalidate)
   - **Mitigation**: For MVP, we use TanStack Query on client side (simpler)

4. **Bundle Size**
   - Next.js adds framework overhead (~80KB gzipped for framework)
   - Larger than Vite + React (~40KB)
   - **Mitigation**: Negligible for modern networks; benefits outweigh cost

5. **Vendor Lock-in (Mild)**
   - Next.js has Vercel-specific features (Edge Runtime, Middleware)
   - But core features work on any Node.js host
   - **Mitigation**: We can deploy anywhere (Netlify, Railway, self-hosted); not locked in

### Neutral 🔄

1. **API Routes (NOT Used)**
   - Next.js has API routes (`app/api/`)
   - We're using Spring Boot backend, so we don't need them
   - Could use for image proxy or BFF pattern later

2. **SSR vs CSR**
   - App Router defaults to SSR (server-side rendering)
   - For our app, most pages are protected (need auth)
   - We'll use mostly client-side fetching with TanStack Query
   - SSR mostly benefits public pages (landing page)

## Why Next.js 14+ App Router Fits Our Project

### 1. Dashboard Pages (Server + Client Components)
```tsx
// app/(dashboard)/citizen/issues/page.tsx
import { IssueList } from '@/components/features/IssueList'; // Client Component

export default function CitizenIssuesPage() {
  return (
    <div>
      <h1>My Issues</h1>
      {/* IssueList is a Client Component (uses TanStack Query) */}
      <IssueList />
    </div>
  );
}
```

### 2. Route Groups for Organization
```
app/
├── (auth)/              # Login, Register (no dashboard layout)
│   ├── login/page.tsx
│   └── register/page.tsx
│
├── (dashboard)/         # Protected pages (with dashboard layout)
│   ├── layout.tsx       # Sidebar, header for all dashboards
│   ├── admin/
│   │   ├── page.tsx     # Admin dashboard
│   │   └── users/
│   │       └── page.tsx # User management
│   ├── staff/
│   │   └── page.tsx     # Staff dashboard
│   └── citizen/
│       ├── page.tsx     # Citizen dashboard
│       └── issues/
│           ├── page.tsx         # My issues
│           ├── new/page.tsx     # Create new issue
│           └── [id]/page.tsx    # Issue details
```

### 3. Protected Routes (Middleware)
```typescript
// middleware.ts
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const token = request.cookies.get('token')?.value;

  // Redirect to login if not authenticated
  if (!token && request.nextUrl.pathname.startsWith('/dashboard')) {
    return NextResponse.redirect(new URL('/login', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/admin/:path*', '/staff/:path*', '/citizen/:path*'],
};
```

### 4. Type-Safe Forms with Shadcn UI
```tsx
"use client"; // Client Component (interactive form)

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

const loginSchema = z.object({
  email: z.string().email('Invalid email'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

type LoginForm = z.infer<typeof loginSchema>;

export function LoginForm() {
  const { register, handleSubmit, formState: { errors } } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginForm) => {
    // Call Spring Boot API
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    // Handle response...
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Input {...register('email')} placeholder="Email" />
      {errors.email && <p>{errors.email.message}</p>}

      <Input {...register('password')} type="password" placeholder="Password" />
      {errors.password && <p>{errors.password.message}</p>}

      <Button type="submit">Login</Button>
    </form>
  );
}
```

## Comparison: App Router vs Pages Router

| Feature | App Router (Next.js 14+) | Pages Router (Next.js 12) |
|---------|-------------------------|--------------------------|
| **Routing** | File-based (`app/` folder) | File-based (`pages/` folder) |
| **Server Components** | ✅ Default | ❌ Not available |
| **Layouts** | Nested layouts (shared UI) | Manual layout components |
| **Loading States** | `loading.tsx` (automatic) | Manual loading components |
| **Error Handling** | `error.tsx` (boundary) | Manual error boundaries |
| **Data Fetching** | Fetch in Server Components | getServerSideProps, getStaticProps |
| **Metadata** | `metadata` export | `<Head>` component |
| **Route Groups** | ✅ `(group-name)/` | ❌ Not available |
| **Streaming** | ✅ Suspense support | ❌ Limited |
| **Future-Proof** | ✅ Recommended by Next.js | 🟡 Stable but older |
| **Learning Resources** | Growing (2023+) | Mature (2018+) |

**Decision**: App Router is the future. Pages Router is in maintenance mode.

## Integration with Spring Boot Backend

Next.js frontend will consume Spring Boot REST API:

```typescript
// lib/api.ts
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export async function apiClient<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  const token = localStorage.getItem('token');

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options?.headers,
    },
  });

  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }

  return response.json();
}

// Usage
const issues = await apiClient<Issue[]>('/issues');
```

## Deployment

- **Development**: `npm run dev` (http://localhost:3000)
- **Production**: Vercel (free tier, one-click deployment)
- **Alternative**: Netlify, Railway, Docker + Node.js server

## Implementation Checklist

Sprint 1 (Week 1):
- [ ] Initialize Next.js 14+ with App Router (`npx create-next-app@latest`)
- [ ] Configure TypeScript (strict mode)
- [ ] Install Shadcn UI (`npx shadcn-ui@latest init`)
- [ ] Install Tailwind CSS (included with Next.js setup)
- [ ] Set up folder structure (app/, components/, lib/, types/)
- [ ] Create route groups `(auth)` and `(dashboard)`
- [ ] Implement login/register pages
- [ ] Configure API client (lib/api.ts)
- [ ] Set up environment variables (.env.local)
- [ ] Add protected route middleware

Sprint 2-3:
- [ ] Create dashboard layouts (admin, staff, citizen)
- [ ] Implement issue list pages
- [ ] Create issue form (React Hook Form + Zod)
- [ ] Add image upload functionality
- [ ] Implement filtering and sorting

## References

- [Next.js 14 Documentation](https://nextjs.org/docs)
- [Next.js App Router](https://nextjs.org/docs/app)
- [React Server Components](https://react.dev/blog/2023/03/22/react-labs-what-we-have-been-working-on-march-2023#react-server-components)
- [Shadcn UI](https://ui.shadcn.com/)
- [TypeScript with Next.js](https://nextjs.org/docs/app/building-your-application/configuring/typescript)
