import { useEffect, useState } from 'react';
import { useRouter } from 'next/router';
import Head from 'next/head';
import Layout from '../components/Layout';
import { getUsers, addUser } from '../utils/api';
import { createAvatar, setCurrentUserId, clearCurrentUserId } from '../utils/app';

export default function Index() {
    const router = useRouter();
    const [allUsers, setAllUsers] = useState([]);
    const [displayUsers, setDisplayUsers] = useState({ hubs: [], regulars: [] });
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    
    // Form state
    const [name, setName] = useState('');
    const [age, setAge] = useState(18);
    const [bio, setBio] = useState('');

    useEffect(() => {
        clearCurrentUserId();
        loadUsers();
    }, []);

    const loadUsers = async () => {
        try {
            setLoading(true);
            const data = await getUsers();
            const users = data || [];
            setAllUsers(users);
            
            const hubs = users.filter(u => (u.friends ? u.friends.length : 0) >= 100);
            const regulars = users.filter(u => (u.friends ? u.friends.length : 0) < 100);
            
            setDisplayUsers({ 
                hubs: hubs.slice(0, 6), 
                regulars: regulars.slice(0, 6) 
            });
        } catch (error) {
            console.error("Lỗi lấy danh sách user:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!allUsers.length) return;
        
        const term = searchTerm.toLowerCase();
        let filtered = allUsers;
        
        if (term) {
            filtered = allUsers.filter(u => u.displayName.toLowerCase().includes(term));
        }
        
        const hubs = filtered.filter(u => (u.friends ? u.friends.length : 0) >= 100);
        const regulars = filtered.filter(u => (u.friends ? u.friends.length : 0) < 100);
        
        setDisplayUsers({ 
            hubs: hubs.slice(0, 6), 
            regulars: regulars.slice(0, 6) 
        });
    }, [searchTerm, allUsers]);

    const handleSelectUser = (id) => {
        setCurrentUserId(id);
        router.push('/home');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await addUser({ displayName: name, age: parseInt(age), bio });
            setIsModalOpen(false);
            setName('');
            setAge(18);
            setBio('');
            loadUsers();
        } catch (error) {
            alert('Có lỗi xảy ra khi thêm người dùng.');
        }
    };

    const renderUserList = (users) => {
        if (users.length === 0) return <div className="empty-state">Không tìm thấy</div>;
        
        return users.map(user => {
            const friendCount = user.friends ? user.friends.length : 0;
            const isHub = friendCount >= 100;
            return (
                <div 
                    key={user.id} 
                    className="card user-card" 
                    onClick={() => handleSelectUser(user.id)}
                    style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '1rem', position: 'relative' }}
                >
                    {isHub && (
                        <div style={{
                            position: 'absolute', top: '10px', right: '10px', 
                            background: 'var(--gold)', color: '#000', 
                            padding: '2px 8px', borderRadius: '12px', 
                            fontSize: '0.7rem', fontWeight: 'bold',
                            boxShadow: '0 2px 10px rgba(201,169,110,0.5)'
                        }}>
                            👑 Hub
                        </div>
                    )}
                    <div className="user-avatar" style={{ width: '48px', height: '48px', fontSize: '1.2rem', backgroundColor: user.avatarColor || 'var(--primary)', border: isHub ? '2px solid var(--gold)' : 'none', flexShrink: 0 }}>
                        {createAvatar(user.displayName)}
                    </div>
                    <div style={{ minWidth: 0, overflow: 'hidden' }}>
                        <h3 className="user-name" style={{ fontSize: '1.1rem', marginBottom: '0.2rem', color: isHub ? 'var(--gold)' : 'inherit', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                            {user.displayName}
                        </h3>
                        <div className="user-meta" style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
                            <span>{user.age || '--'} tuổi</span>
                            <span>•</span>
                            <span>ID: {user.id.substring(0, 4)}</span>
                            <span>•</span>
                            <span style={{ color: isHub ? 'var(--gold)' : 'var(--muted)', fontWeight: isHub ? 'bold' : 'normal' }}>
                                {friendCount} bạn bè
                            </span>
                        </div>
                    </div>
                </div>
            );
        });
    };

    return (
        <Layout showNav={false}>
            <Head>
                <title>Social Network - Select User</title>
            </Head>

            <div style={{ maxWidth: '1200px', margin: '0 auto', paddingTop: '4rem' }}>
                <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
                    <h2 style={{ fontFamily: "'Cormorant Garamond', serif", fontSize: '2.5rem', color: 'var(--gold)', marginBottom: '1rem' }}>
                        Đăng nhập & Chọn người dùng
                    </h2>
                    <p style={{ color: 'var(--muted)', fontSize: '1.1rem' }}>
                        Chọn một tài khoản có sẵn trong hệ thống ({allUsers.length} users) hoặc tạo mới
                    </p>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
                    <div style={{ flex: 1, maxWidth: '400px' }}>
                        <input 
                            type="text" 
                            className="input-control" 
                            placeholder="Tìm kiếm theo tên..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                    <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
                        + Thêm người dùng mới
                    </button>
                </div>

                {loading ? (
                    <div className="empty-state">Đang tải...</div>
                ) : (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '2rem' }}>
                        {/* Regular Users Column */}
                        <div>
                            <h3 style={{ color: 'var(--muted)', marginBottom: '1.5rem', borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '0.5rem' }}>
                                👤 Tài khoản thường (&lt; 100 bạn)
                            </h3>
                            <div className="grid" style={{ gridTemplateColumns: '1fr', gap: '1rem' }}>
                                {renderUserList(displayUsers.regulars)}
                            </div>
                        </div>

                        {/* Hub Users Column */}
                        <div>
                            <h3 style={{ color: 'var(--gold)', marginBottom: '1.5rem', borderBottom: '1px solid rgba(201,169,110,0.3)', paddingBottom: '0.5rem' }}>
                                👑 Tài khoản Hub (≥ 100 bạn)
                            </h3>
                            <div className="grid" style={{ gridTemplateColumns: '1fr', gap: '1rem' }}>
                                {renderUserList(displayUsers.hubs)}
                            </div>
                        </div>
                    </div>
                )}
                
                {!loading && (displayUsers.hubs.length > 0 || displayUsers.regulars.length > 0) && searchTerm === '' && allUsers.length > 12 && (
                    <div style={{ textAlign: 'center', marginTop: '3rem' }}>
                        <p style={{ color: 'var(--muted)', fontSize: '0.9rem', fontFamily: "'DM Mono'" }}>
                            Dùng ô tìm kiếm để xem thêm các tài khoản khác.
                        </p>
                    </div>
                )}
            </div>

            {isModalOpen && (
                <div className="modal-overlay active">
                    <div className="modal">
                        <h3 style={{ marginBottom: '1.5rem', color: 'var(--gold)' }}>Thêm người dùng mới</h3>
                        <form onSubmit={handleSubmit}>
                            <div className="input-group">
                                <label>Tên hiển thị</label>
                                <input 
                                    type="text" 
                                    className="input-control" 
                                    required 
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                />
                            </div>
                            <div className="input-group">
                                <label>Tuổi</label>
                                <input 
                                    type="number" 
                                    className="input-control" 
                                    required min="13" max="120"
                                    value={age}
                                    onChange={(e) => setAge(e.target.value)}
                                />
                            </div>
                            <div className="input-group">
                                <label>Tiểu sử</label>
                                <input 
                                    type="text" 
                                    className="input-control"
                                    value={bio}
                                    onChange={(e) => setBio(e.target.value)}
                                />
                            </div>
                            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
                                <button type="button" className="btn" style={{ flex: 1 }} onClick={() => setIsModalOpen(false)}>Hủy</button>
                                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>Thêm</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </Layout>
    );
}
