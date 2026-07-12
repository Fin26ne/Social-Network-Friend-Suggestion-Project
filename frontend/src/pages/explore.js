import { useEffect, useState } from 'react';
import Head from 'next/head';
import Layout from '../components/Layout';
import { getUsers, getFriends, addFriend } from '../utils/api';
import { getCurrentUserId, createAvatar } from '../utils/app';

export default function Explore() {
    const [filteredUsers, setFilteredUsers] = useState([]);
    const [displayUsers, setDisplayUsers] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const userId = getCurrentUserId();
        if (userId) {
            initData(userId);
        }
    }, []);

    const initData = async (userId) => {
        try {
            setLoading(true);
            const [allUsers, currentFriends] = await Promise.all([
                getUsers(),
                getFriends(userId)
            ]);

            const friendsSet = new Set((currentFriends || []).map(f => f.id));
            const available = (allUsers || []).filter(u => u.id !== userId && !friendsSet.has(u.id));
            
            setFilteredUsers(available);
            setDisplayUsers(available);
        } catch (error) {
            console.error("Lỗi tải dữ liệu explore:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const timeout = setTimeout(() => {
            const term = searchTerm.toLowerCase();
            const results = filteredUsers.filter(u => u.displayName.toLowerCase().includes(term));
            setDisplayUsers(results);
        }, 300);
        return () => clearTimeout(timeout);
    }, [searchTerm, filteredUsers]);

    const handleAddFriend = async (targetId) => {
        const userId = getCurrentUserId();
        try {
            await addFriend(userId, targetId);
            initData(userId); // Refresh data
        } catch (err) {
            alert('Lỗi thêm bạn');
        }
    };

    return (
        <Layout>
            <Head>
                <title>Social Network - Khám phá</title>
            </Head>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '2rem' }}>
                <div>
                    <h2 style={{ marginBottom: '0.5rem', color: 'var(--gold)' }}>Khám phá cộng đồng</h2>
                    <p style={{ color: 'var(--muted)', fontSize: '0.9rem' }}>Tìm kiếm và kết nối với những người dùng khác</p>
                </div>
                <div style={{ width: '300px' }}>
                    <input 
                        type="text" 
                        className="input-control" 
                        placeholder="Tìm kiếm tên..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
            </div>

            <div className="grid">
                {loading ? (
                    <div className="empty-state">Đang tải...</div>
                ) : displayUsers.length === 0 ? (
                    <div className="empty-state" style={{ gridColumn: '1 / -1' }}>Không tìm thấy người dùng phù hợp</div>
                ) : (
                    displayUsers.map(u => (
                        <div key={u.id} className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                <div className="user-avatar" style={{ width: '48px', height: '48px', fontSize: '1.2rem', backgroundColor: u.avatarColor || 'var(--primary)' }}>
                                    {createAvatar(u.displayName)}
                                </div>
                                <div>
                                    <h3 className="user-name" style={{ fontSize: '1.1rem', marginBottom: '0.2rem' }}>{u.displayName}</h3>
                                    <div className="user-meta">{u.age || '--'} tuổi</div>
                                </div>
                            </div>
                            {u.bio && <div style={{ fontSize: '0.85rem', color: 'var(--muted)' }}>{u.bio}</div>}
                            <div style={{ fontFamily: "'DM Mono'", fontSize: '0.8rem', color: 'var(--goldglow)' }}>
                                ? bạn chung
                            </div>
                            <button 
                                className="btn btn-primary btn-add" 
                                style={{ width: '100%', marginTop: 'auto' }}
                                onClick={() => handleAddFriend(u.id)}
                            >
                                Thêm bạn bè
                            </button>
                        </div>
                    ))
                )}
            </div>
        </Layout>
    );
}
