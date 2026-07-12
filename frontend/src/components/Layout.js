import Link from 'next/link';
import { useRouter } from 'next/router';
import { useEffect, useState } from 'react';
import { getUser } from '../utils/api';
import { getCurrentUserId, createAvatar } from '../utils/app';

export default function Layout({ children, showNav = true }) {
    const router = useRouter();
    const [currentUser, setCurrentUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function fetchUser() {
            const userId = getCurrentUserId();
            if (!userId && showNav) {
                router.push('/');
                return;
            }
            if (userId) {
                try {
                    const user = await getUser(userId);
                    setCurrentUser(user);
                } catch (error) {
                    console.error("Lỗi lấy thông tin user:", error);
                }
            }
            setLoading(false);
        }
        fetchUser();
    }, [router, showNav]);

    return (
        <>
            <div className="grain"></div>
            
            <div className="topbar">
                <div className="brand">SocialNet</div>
                {showNav && (
                    <>
                        <div className="nav-links">
                            <Link href="/home" className={router.pathname === '/home' ? 'active' : ''}>Trang chủ</Link>
                            <Link href="/explore" className={router.pathname === '/explore' ? 'active' : ''}>Khám phá</Link>
                            <Link href="/network" className={router.pathname === '/network' ? 'active' : ''}>Mạng lưới</Link>
                            <Link href="/research" className={router.pathname === '/research' ? 'active' : ''}>Nghiên cứu</Link>
                        </div>
                        <div className="nav-user">
                            <span id="current-user-name">
                                {loading ? 'Đang tải...' : (currentUser?.displayName || 'Unknown')}
                            </span>
                            <div id="current-user-avatar" className="user-avatar">
                                {loading ? '?' : createAvatar(currentUser?.displayName)}
                            </div>
                            <Link href="/" className="btn btn-sm" style={{ marginLeft: '1rem' }}>Đổi tài khoản</Link>
                        </div>
                    </>
                )}
            </div>

            <div className="container">
                {children}
            </div>
        </>
    );
}
