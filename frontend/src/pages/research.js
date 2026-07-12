import { useEffect, useState, useRef } from 'react';
import Head from 'next/head';
import Layout from '../components/Layout';

const mockData = {
    rq2: [
        { size: 100, edges: 10, listMemKb: 8.28, matrixMemKb: 10.58, winner: 'List' },
        { size: 500, edges: 250, listMemKb: 50.78, matrixMemKb: 248.08, winner: 'List' },
        { size: 1000, edges: 1000, listMemKb: 125.0, matrixMemKb: 984.4, winner: 'List' },
        { size: 2000, edges: 4000, listMemKb: 343.75, matrixMemKb: 3921.9, winner: 'List' },
        { size: 5000, edges: 25000, listMemKb: 1562.5, matrixMemKb: 24453.15, winner: 'List' }
    ]
};

function formatNum(n) { return new Intl.NumberFormat().format(n); }

export default function Research() {
    const [sizes, setSizes] = useState("1000,2000,3000,4000,5000");
    const [density, setDensity] = useState(0.001);
    const [mode, setMode] = useState('theory');
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    
    // Canvas ref
    const canvasRef = useRef(null);

    useEffect(() => {
        fetchBenchmark();
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    const fetchBenchmark = async () => {
        setLoading(true);
        try {
            const res = await fetch(`http://localhost:3003/api/suggestions/benchmark?type=rq2&sizes=${sizes}&edgeDensity=${density}&mode=${mode}`);
            if (!res.ok) throw new Error('Benchmark failed');
            const result = await res.json();
            const list = result.data?.benchmarkResults || result.data?.rq2 || mockData.rq2;
            setData(list);
        } catch (err) {
            console.warn('Using mock data for benchmarks', err);
            setData(mockData.rq2);
        } finally {
            setLoading(false);
        }
    };

    // Debounce effect
    useEffect(() => {
        const timeoutId = setTimeout(() => {
            if (sizes && density) fetchBenchmark();
        }, 800);
        return () => clearTimeout(timeoutId);
    }, [sizes, density, mode]); // eslint-disable-line react-hooks/exhaustive-deps

    return (
        <Layout>
            <Head>
                <title>Social Network - Research</title>
                <style>{`
                    .hero-title { font-family: 'Cormorant Garamond', serif; font-size: 48px; font-style: italic; color: var(--gold); margin-bottom: 0.5rem; }
                    .hero-subtitle { color: var(--muted); font-size: 1.1rem; margin-bottom: 2rem; }
                    .badges-row { display: flex; gap: 1rem; margin-bottom: 3rem; }
                    .badge { font-family: 'DM Mono', monospace; font-size: 12px; padding: 4px 12px; background: rgba(201, 169, 110, 0.1); color: var(--gold); border: 1px solid rgba(201, 169, 110, 0.3); border-radius: 4px; }
                    .section-number { font-family: 'DM Mono', monospace; color: rgba(201, 169, 110, 0.4); font-size: 14px; margin-bottom: 0.5rem; }
                    .chart-container { background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 24px; position: relative; }
                    .conclusion-box { border-left: 3px solid var(--gold); background: var(--goldglow); padding: 16px; border-radius: 0 12px 12px 0; margin-top: 1.5rem; font-size: 0.95rem; line-height: 1.5; }
                    table { width: 100%; border-collapse: collapse; margin-top: 1.5rem; font-size: 0.9rem; }
                    th { text-align: left; padding: 12px; border-bottom: 1px solid var(--border); color: var(--muted); font-weight: 500; }
                    td { padding: 12px; border-bottom: 1px solid rgba(255, 255, 255, 0.05); }
                    tr:nth-child(even) { background: rgba(255, 255, 255, 0.02); }
                    .layout-grid { display: grid; grid-template-columns: 1fr 2fr; gap: 2rem; align-items: start; }
                    .layout-grid.reverse { grid-template-columns: 2fr 1fr; }
                `}</style>
            </Head>

            <div style={{ paddingBottom: '5rem' }}>
                <div style={{ marginBottom: '3rem' }}>
                    <h1 className="hero-title">Research & Analysis</h1>
                    <div className="hero-subtitle">Topic 05 — Social Network Friend Suggestion | CSD201 FPT</div>
                    <div className="badges-row">
                        <div className="badge">Phân tích hiệu năng bộ nhớ</div>
                        <div className="badge">Adjacency List vs Adjacency Matrix</div>
                    </div>
                </div>

                <div className="card" style={{ padding: '2rem', marginBottom: '3rem', background: 'var(--bg2)', border: '1px solid var(--border)', borderRadius: '16px', maxWidth: '600px', margin: '0 auto 3rem auto' }}>
                    <h3 style={{ color: 'var(--gold)', marginBottom: '1.5rem', fontFamily: "'Cormorant Garamond', serif", fontSize: '1.8rem', fontStyle: 'italic', textAlign: 'center' }}>
                        Cấu hình đo lường hiệu năng bộ nhớ
                    </h3>
                    
                    <div>
                        <div style={{ marginBottom: '1.5rem' }}>
                            <label style={{ display: 'block', fontSize: '13px', color: 'var(--muted)', marginBottom: '6px' }}>Kích thước nút (N):</label>
                            <input type="text" className="input-control" value={sizes} onChange={e => setSizes(e.target.value)} style={{ width: '100%', fontFamily: "'DM Mono'", padding: '10px' }} />
                        </div>
                        <div style={{ marginBottom: '1.5rem' }}>
                            <label style={{ display: 'block', fontSize: '13px', color: 'var(--muted)', marginBottom: '6px' }}>Mật độ cạnh (Density):</label>
                            <input type="number" className="input-control" value={density} step="0.0001" min="0.0001" max="0.2" onChange={e => setDensity(e.target.value)} style={{ width: '100%', fontFamily: "'DM Mono'", padding: '10px' }} />
                        </div>
                        <div style={{ marginBottom: '1.5rem' }}>
                            <label style={{ display: 'block', fontSize: '13px', color: 'var(--muted)', marginBottom: '6px' }}>Chế độ đo lường:</label>
                            <select className="input-control" value={mode} onChange={e => setMode(e.target.value)} style={{ width: '100%', fontFamily: "'DM Mono'", padding: '10px' }}>
                                <option value="theory">Lý thuyết (Toán học O(1))</option>
                                <option value="actual">Thực tế (Cấp phát RAM - Max 500k)</option>
                            </select>
                        </div>
                        <button className="btn btn-primary" style={{ width: '100%' }} onClick={fetchBenchmark} disabled={loading}>
                            {loading ? 'Đang tính toán...' : 'Chạy thử nghiệm'}
                        </button>
                    </div>
                </div>

                <div className="section-number">01 — PHÂN TÍCH BỘ NHỚ LƯU TRỮ</div>
                <h2 style={{ marginBottom: '2rem', color: 'var(--gold)' }}>Hiệu năng bộ nhớ: Adjacency List vs Adjacency Matrix</h2>
                
                <div className="layout-grid reverse" style={{ marginBottom: '3rem' }}>
                    <div>
                        <h4 style={{ color: 'var(--gold)', marginBottom: '1rem', fontFamily: "'DM Mono', monospace", textTransform: 'uppercase' }}>
                            Bảng đo lường {mode === 'theory' ? 'Lý thuyết' : 'Thực tế'}
                        </h4>
                        <table>
                            <thead>
                                <tr>
                                    <th>Số Node (N)</th>
                                    <th>Số cạnh (E)</th>
                                    <th>List (KB)</th>
                                    <th>Matrix (KB)</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data.map((d, i) => (
                                    <tr key={i}>
                                        <td>{formatNum(d.size)}</td>
                                        <td>{formatNum(d.edges)}</td>
                                        <td style={{ color: '#C9A96E' }}>{formatNum(Math.round(mode === 'theory' ? d.listMemKb : d.actualListMemKb))} KB</td>
                                        <td style={{ color: '#E08F8F' }}>{formatNum(Math.round(mode === 'theory' ? d.matrixMemKb : d.actualMatrixMemKb))} KB</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    <div>
                        <div className="chart-container" style={{ padding: '16px' }}>
                            <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', justifyContent: 'center', fontSize: '0.8rem', fontFamily: "'DM Mono', monospace" }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><div style={{ width:'12px', height:'2px', background:'#C9A96E' }}></div> Danh sách kề (KB)</div>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><div style={{ width:'12px', height:'2px', background:'#E08F8F' }}></div> Ma trận kề (KB)</div>
                            </div>
                            <div className="empty-state" style={{ height: '300px' }}>
                                Biểu đồ đã được ẩn trong phiên bản React để tối ưu hiển thị
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
}
