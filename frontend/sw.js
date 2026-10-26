// ═══════════════════════════════════════════════════
// MINISTER OFFICE ERP - SERVICE WORKER
// PWA Offline Support & Caching
// ═══════════════════════════════════════════════════

const CACHE_NAME = 'minister-erp-v1.0.0';
const STATIC_CACHE = 'minister-static-v1';
const API_CACHE = 'minister-api-v1';

// ── Files to cache for offline use
const STATIC_FILES = [
    '/',
    '/operator/index.html',
    '/operator/dashboard.html',
    '/operator/new-complaint.html',
    '/operator/complaints.html',
    '/officer/index.html',
    '/officer/dashboard.html',
    '/officer/my-complaints.html',
    '/minister/index.html',
    '/minister/dashboard.html',
    '/minister/all-complaints.html',
    '/minister/users.html',
    '/minister/analytics.html',
    '/assets/css/main.css',
    '/assets/css/dashboard.css',
    '/assets/css/components.css',
    '/assets/js/api.js',
    '/assets/js/auth.js',
    '/assets/js/utils.js',
    '/assets/js/complaints.js',
    '/assets/js/charts.js',
    '/manifest.json',
    'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css',
    'https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap',
    'https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js'
];

// ── Install Event
self.addEventListener('install', (event) => {
    console.log(
        '[SW] Installing Service Worker...');

    event.waitUntil(
        caches.open(STATIC_CACHE)
            .then((cache) => {
                console.log(
                    '[SW] Caching static files');
                return cache.addAll(
                    STATIC_FILES
                    .filter(url =>
                        !url.startsWith('http'))
                );
            })
            .then(() => {
                console.log(
                    '[SW] Installation complete');
                return self.skipWaiting();
            })
            .catch((error) => {
                console.log(
                    '[SW] Cache failed:',
                    error);
            })
    );
});

// ── Activate Event
self.addEventListener('activate', (event) => {
    console.log(
        '[SW] Activating Service Worker...');

    event.waitUntil(
        Promise.all([
            // Delete old caches
            caches.keys().then((cacheNames) => {
                return Promise.all(
                    cacheNames
                    .filter(name =>
                        name !== STATIC_CACHE
                        && name !== API_CACHE)
                    .map(name => {
                        console.log(
                            '[SW] Deleting cache:',
                            name);
                        return caches.delete(name);
                    })
                );
            }),
            // Take control immediately
            self.clients.claim()
        ])
    );
});

// ── Fetch Event
self.addEventListener('fetch', (event) => {
    const url = new URL(event.request.url);

    // Skip non-GET requests
    if (event.request.method !== 'GET') {
        return;
    }

    // Skip API calls - always fetch fresh
    if (url.pathname.startsWith('/api/')) {
        event.respondWith(
            networkFirstStrategy(
                event.request));
        return;
    }

    // Static files - cache first
    event.respondWith(
        cacheFirstStrategy(event.request));
});

// ── Cache First Strategy
async function cacheFirstStrategy(request) {
    try {
        const cachedResponse =
            await caches.match(request);

        if (cachedResponse) {
            // Return cached version
            // and update in background
            fetchAndCache(request);
            return cachedResponse;
        }

        // Not in cache - fetch from network
        return await fetchAndCache(request);

    } catch (error) {
        // Return offline page if available
        return await caches.match(
            '/operator/index.html');
    }
}

// ── Network First Strategy
async function networkFirstStrategy(
        request) {
    try {
        const networkResponse =
            await fetch(request);

        if (networkResponse.ok) {
            const cache = await caches.open(
                API_CACHE);
            cache.put(request,
                networkResponse.clone());
        }

        return networkResponse;

    } catch (error) {
        // Return cached API response
        // if network fails
        const cachedResponse =
            await caches.match(request);

        if (cachedResponse) {
            return cachedResponse;
        }

        // Return error response
        return new Response(
            JSON.stringify({
                success: false,
                message: 'You are offline. '
                    + 'Please check your '
                    + 'internet connection.',
                offline: true
            }),
            {
                headers: {
                    'Content-Type':
                        'application/json'
                }
            }
        );
    }
}

// ── Fetch and Cache Helper
async function fetchAndCache(request) {
    const networkResponse =
        await fetch(request);

    if (networkResponse.ok) {
        const cache = await caches.open(
            STATIC_CACHE);
        cache.put(request,
            networkResponse.clone());
    }

    return networkResponse;
}

// ── Background Sync
self.addEventListener('sync', (event) => {
    if (event.tag === 'sync-complaints') {
        event.waitUntil(syncComplaints());
    }
});

// ── Sync complaints when back online
async function syncComplaints() {
    try {
        console.log(
            '[SW] Syncing offline data...');
        // Implement offline sync logic here
    } catch (error) {
        console.log('[SW] Sync failed:', error);
    }
}

// ── Push Notifications
self.addEventListener('push', (event) => {
    if (!event.data) return;

    const data = event.data.json();

    const options = {
        body: data.message
            || 'New notification',
        icon: '/assets/img/icon-192.png',
        badge: '/assets/img/icon-72.png',
        tag: data.type || 'notification',
        data: {
            url: data.url
                || '/operator/dashboard.html'
        },
        actions: [
            {
                action: 'view',
                title: 'View',
                icon: '/assets/img/icon-72.png'
            },
            {
                action: 'dismiss',
                title: 'Dismiss'
            }
        ],
        requireInteraction: true,
        vibrate: [200, 100, 200]
    };

    event.waitUntil(
        self.registration.showNotification(
            data.title
            || 'Minister Office ERP',
            options
        )
    );
});

// ── Notification Click
self.addEventListener(
    'notificationclick', (event) => {
    event.notification.close();

    if (event.action === 'dismiss') return;

    const url = event.notification.data?.url
        || '/operator/dashboard.html';

    event.waitUntil(
        clients.matchAll({
            type: 'window',
            includeUncontrolled: true
        }).then((windowClients) => {
            // Focus existing window
            for (const client
                    of windowClients) {
                if (client.url === url
                        && 'focus'
                        in client) {
                    return client.focus();
                }
            }
            // Open new window
            if (clients.openWindow) {
                return clients.openWindow(url);
            }
        })
    );
});

console.log(
    '[SW] Minister Office ERP '
    + 'Service Worker Loaded ✅');