import { useEffect, useState } from 'react';
import Head from 'next/head';
import Layout from '../components/Layout';
import { getNetworkData } from '../utils/api';
import { renderNetworkGraph } from '../utils/network';
import { getCurrentUserId } from '../utils/app';

export default function Network() {
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        loadNetwork();
    }, []);

    const loadNetwork = async () => {
        try {
            setLoading(true);
            const userId = getCurrentUserId();
            const data = await getNetworkData(userId);
            
            setTimeout(() => {
                renderNetworkGraph('fullscreen-network', data, null, userId);
            }, 100);
        } catch (err) {
            console.error(err);
            setError("Lỗi tải mạng lưới");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Layout>
            <Head>
                <title>Social Network - Mạng lưới</title>
            </Head>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '2rem' }}>
                <div>
                    <h2 style={{ marginBottom: '0.5rem', color: 'var(--gold)' }}>Đồ thị mạng lưới</h2>
                    <p style={{ color: 'var(--muted)', fontSize: '0.9rem' }}>Trực quan hóa cấu trúc dữ liệu đồ thị (Graph)</p>
                </div>
            </div>

            <div className="card" style={{ height: 'calc(100vh - 250px)', padding: '1rem' }}>
                {loading && <div className="empty-state">Đang tải bản đồ mạng lưới...</div>}
                {error && <div className="empty-state">{error}</div>}
                <div id="fullscreen-network" style={{ width: '100%', height: '100%' }}></div>
            </div>
        </Layout>
    );
}
