import { useEffect, useState } from 'react';
import Head from 'next/head';
import Layout from '../components/Layout';
import { getUser, getUsers, getSuggestions, getFriends, addFriend, getNetworkData } from '../utils/api';
import { getCurrentUserId, createAvatar } from '../utils/app';
import { renderNetworkGraph } from '../utils/network';
import Link from 'next/link';

export default function Home() {
    const [stats, setStats] = useState({ users: 0, friends: 0, maxScore: 0 });
    const [friends, setFriends] = useState([]);
    const [suggestions, setSuggestions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const userId = getCurrentUserId();
        if (userId) {
            initData(userId);
        }
    }, []);

    const initData = async (userId) => {
        try {
            setLoading(true);
            const [allUsers, currentFriends, currentSuggestions] = await Promise.all([
                getUsers(),
                getFriends(userId),
                getSuggestions(userId)
            ]);

            setFriends(currentFriends || []);
            setSuggestions(currentSuggestions || []);

            let maxScore = 0;
            if (currentSuggestions && currentSuggestions.length > 0) {
                maxScore = Math.max(...currentSuggestions.map(s => s.jaccardScore));
            }

            setStats({
                users: allUsers.length,
                friends: currentFriends ? currentFriends.length : 0,
                maxScore
            });

            loadNetwork(userId);
        } catch (err) {
            console.error("Failed to init home page", err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const loadNetwork = async (userId) => {
        try {
            const data = await getNetworkData(userId);
            // Need a small timeout to ensure container is rendered
            setTimeout(() => {
                renderNetworkGraph('network-container', data, null, userId);
            }, 100);
        } catch (err) {
            console.error("Network graph error:", err);
        }
    };

    const handleAddFriend = async (targetId) => {
        const userId = getCurrentUserId();
        try {
            await addFriend(userId, targetId);
            initData(userId); // Refresh data
        } catch (err) {
            alert('Lỗi thêm bạn: ' + err.message);
        }
    };

    return (
        <Layout>
            <Head>
                <title>Social Network - Dashboard</title>
            </Head>

            <div className="stats-row">
                <div className="stat-card">
                    <div className="stat-value">{loading ? '-' : stats.users}</div>
                    <div className="stat-label">Tổng người dùng</div>
                </div>
                <div className="stat-card">
                    <div className="stat-value">{loading ? '-' : stats.friends}</div>
                    <div className="stat-label">Bạn bè của bạn</div>
                </div>
                <div className="stat-card">
                    <div className="stat-value">{loading ? '-' : stats.maxScore.toFixed(4)}</div>
                    <div className="stat-label">Điểm Jaccard cao nhất</div>
                </div>
            </div>

            <div className="dashboard-layout">
                <div className="sidebar">
                    <div className="card" style={{ flex: 1 }}>
                        <div className="section-title">Danh sách bạn bè</div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginTop: '1rem' }}>
                            {loading ? (
                                <div className="empty-state">Đang tải...</div>
                            ) : friends.length === 0 ? (
                                <div className="empty-state">Chưa có bạn bè</div>
                            ) : (
                                friends.map(f => (
                                    <div key={f.id} className="user-item">
                                        <div className="user-avatar" style={{ width: '32px', height: '32px', fontSize: '0.8rem', backgroundColor: f.avatarColor || 'var(--primary)' }}>
                                            {createAvatar(f.displayName)}
                                        </div>
                                        <div className="user-info">
                                            <div className="user-name" style={{ fontSize: '0.9rem' }}>{f.displayName}</div>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
                    <div className="card">
                        <div className="section-title">Gợi ý kết bạn</div>
                        <div style={{ marginTop: '1rem' }}>
                            {loading ? (
                                <div className="empty-state">Đang tải...</div>
                            ) : error ? (
                                <div className="empty-state">Lỗi: {error}</div>
                            ) : suggestions.length === 0 ? (
                                <div className="empty-state">Không có gợi ý nào</div>
                            ) : (
                                suggestions.slice(0, 5).map(s => {
                                    const maxJaccard = stats.maxScore > 0 ? stats.maxScore : 1;
                                    const pct = Math.min(100, Math.max(5, (s.jaccardScore / maxJaccard) * 100));
                                    return (
                                        <div key={s.user.id} className="suggestion-card">
                                            <div className="user-avatar" style={{ backgroundColor: s.user.avatarColor || 'var(--primary)' }}>
                                                {createAvatar(s.user.displayName)}
                                            </div>
                                            <div className="user-info">
                                                <div className="user-name">{s.user.displayName}</div>
                                                <div className="user-meta">{s.mutualCount} bạn chung</div>
                                                <div className="score-bar-container">
                                                    <div className="score-bar" style={{ width: `${pct}%` }}></div>
                                                </div>
                                            </div>
                                            <div style={{ fontFamily: "'DM Mono'", fontSize: '0.8rem', color: 'var(--gold)', marginRight: '1rem' }}>
                                                {s.jaccardScore.toFixed(4)}
                                            </div>
                                            <button 
                                                className="btn btn-sm btn-primary" 
                                                onClick={() => handleAddFriend(s.user.id)}
                                            >
                                                Thêm
                                            </button>
                                        </div>
                                    );
                                })
                            )}
                        </div>
                    </div>
                    
                    <div className="card">
                        <div className="section-title" style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <span>Mạng lưới lân cận</span>
                            <Link href="/network" style={{ fontSize: '0.8rem', textTransform: 'none', color: 'var(--gold)' }}>
                                &gt; Mở rộng
                            </Link>
                        </div>
                        <div id="network-container" style={{ height: '300px', marginTop: '1rem' }}></div>
                    </div>
                </div>

                <div className="sidebar">
                    <div className="card" style={{ flex: 1 }}>
                        <div className="section-title">Hoạt động gần đây</div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '1rem' }}>
                            <div className="empty-state">Chưa có hoạt động</div>
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
}
