# VOLT V1 — Landing Plan

Premium electric motorcycle landing page. Single file `index.html`, vanilla CSS+JS, no build.

## Design system

- Palette: `#0A0A0A` bg, `#141414` surface, `#0066FF` electric, `#C8FF00` lime, `#F5F5F5` text, `#8A8A8A` muted
- Type: `Instrument Serif` (display), `Inter` (body), `JetBrains Mono` (numbers)
- Spacing: 8pt grid; sections `padding: 120px 24px`
- Radii: 4 / 12 / 24 / full
- Easing: `cubic-bezier(0.22, 1, 0.36, 1)` for reveals; `cubic-bezier(0.65, 0, 0.35, 1)` for UI

## Sections

1. Skip link + sticky nav with scroll progress
2. Hero (gradient mesh, parallax, dual CTA)
3. Press marquee (infinite loop)
4. Stats counters (0-100, range, torque, top speed)
5. Bento grid (6 features)
6. Specs table (reveal rows)
7. Color configurator (5 colors, dynamic accent)
8. Horizontal gallery (snap)
9. Tech deep-dive (battery / motor / AI)
10. Comparison vs Tesla/Zero/LiveWire
11. Press reviews carousel
12. Pricing (3 tiers)
13. FAQ accordion
14. Final CTA (press-and-hold reserve)
15. Footer with newsletter

## Interactions

- Sticky nav blur + hide-on-scroll-down
- Scroll progress bar
- IntersectionObserver reveals + counters
- Marquee infinite
- Color configurator → mutate `--accent`
- Magnetic hover on CTA
- Gallery scroll-snap horizontal
- Carousel auto + dots
- FAQ accordion `aria-expanded`
- Press-and-hold CTA with progress ring
- Custom cursor (desktop only)
- `prefers-reduced-motion` disables parallax / marquee / counters / cursor

## A11y

- Semantic HTML5 landmarks
- `:focus-visible` styles
- Contrast AA+
- Skip-to-content
- Keyboard accordion / configurator / carousel

## Perf

- Inline CSS/JS, no external JS libs
- Fonts: preconnect + `display=swap`
- Images: `loading=lazy decoding=async`
- SVG for icons + logo + motorcycle silhouette
