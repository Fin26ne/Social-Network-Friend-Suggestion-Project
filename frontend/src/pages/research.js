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

    // Draw Chart Effect
    useEffect(() => {
        if (!canvasRef.current || !data || data.length === 0) return;
        const canvas = canvasRef.current;
        const ctx = canvas.getContext('2d');
        
        // Handle High DPI
        const dpr = window.devicePixelRatio || 1;
        const rect = canvas.getBoundingClientRect();
        canvas.width = rect.width * dpr;
        canvas.height = rect.height * dpr;
        ctx.scale(dpr, dpr);
        
        const width = rect.width;
        const height = rect.height;
        
        ctx.clearRect(0, 0, width, height);
        
        const paddingLeft = 75;
        const paddingRight = 30;
        const paddingTop = 40;
        const paddingBottom = 40;
        
        const chartWidth = width - paddingLeft - paddingRight;
        const chartHeight = height - paddingTop - paddingBottom;
        
        const getVal = (d, key) => {
            if (mode === 'theory') return d[key];
            let actual = d['actual' + key.charAt(0).toUpperCase() + key.slice(1)];
            if (actual === 0 && key === 'listMemKb') return d.listMemKb;
            return actual || 0;
        };
        let rawMaxMem = Math.max(1, ...data.map(d => Math.max(getVal(d, 'listMemKb'), getVal(d, 'matrixMemKb'))));
        
        let divisor = 1;
        if (rawMaxMem >= 1048576) divisor = 1048576;
        else if (rawMaxMem >= 1024) divisor = 1024;
        
        let rawMaxInUnit = rawMaxMem / divisor;

        // Calculate nice rounded max memory for Y-axis (ticks) in the display unit
        const roughStep = rawMaxInUnit / 4;
        const stepPower = Math.pow(10, Math.floor(Math.log10(roughStep || 1)));
        const stepFraction = roughStep / stepPower;
        let niceStepFraction = 10;
        if (stepFraction <= 1) niceStepFraction = 1;
        else if (stepFraction <= 2) niceStepFraction = 2;
        else if (stepFraction <= 2.5) niceStepFraction = 2.5;
        else if (stepFraction <= 5) niceStepFraction = 5;
        
        const tickStepInUnit = niceStepFraction * stepPower;
        const maxMem = (tickStepInUnit * 4) * divisor;
        
        const formatYLabel = (val) => {
            if (val >= 1048576) {
                const v = val / 1048576;
                return (v % 1 === 0 ? v : v.toFixed(1)) + ' GB';
            }
            if (val >= 1024) {
                const v = val / 1024;
                return (v % 1 === 0 ? v : v.toFixed(1)) + ' MB';
            }
            return formatNum(Math.round(val)) + ' KB';
        };

        // Draw grid & Y-axis labels
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.1)';
        ctx.fillStyle = 'rgba(255, 255, 255, 0.5)';
        ctx.font = '11px "DM Mono"';
        ctx.textAlign = 'right';
        ctx.textBaseline = 'middle';
        ctx.lineWidth = 1;
        ctx.beginPath();
        for (let i = 0; i <= 4; i++) {
            const y = paddingTop + (4 - i) * chartHeight / 4;
            const val = maxMem * (i / 4);
            
            ctx.moveTo(paddingLeft, y);
            ctx.lineTo(width - paddingRight, y);
            
            // Y-axis label
            ctx.fillText(formatYLabel(val), paddingLeft - 10, y);
        }
        ctx.stroke();

        // X-axis labels
        ctx.fillStyle = 'rgba(255, 255, 255, 0.5)';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'top';
        const xStep = Math.max(1, Math.ceil(data.length / 8));
        data.forEach((d, i) => {
            if (i % xStep === 0 || i === data.length - 1) {
                const x = paddingLeft + (i * (chartWidth / Math.max(1, data.length - 1)));
                const y = height - paddingBottom + 12;
                
                let nLabel = d.size >= 1000000 ? (d.size / 1000000).toFixed(1) + 'M' 
                           : d.size >= 1000 ? (d.size / 1000).toFixed(1) + 'k' 
                           : d.size;
                ctx.fillText(nLabel + ' N', x, y);
            }
        });

        const drawLine = (key, color) => {
            ctx.beginPath();
            ctx.strokeStyle = color;
            ctx.lineWidth = 2;
            let first = true;
            data.forEach((d, i) => {
                const val = getVal(d, key);
                if (val === -1) return; // Không vẽ đường tới điểm bị Tràn RAM
                
                const x = paddingLeft + (i * (chartWidth / Math.max(1, data.length - 1)));
                const y = paddingTop + chartHeight - (val / maxMem) * chartHeight;
                if (first) {
                    ctx.moveTo(x, y);
                    first = false;
                } else {
                    ctx.lineTo(x, y);
                }
            });
            ctx.stroke();
            
            // Draw points
            ctx.fillStyle = color;
            const lblStep = Math.max(1, Math.ceil(data.length / 6));
            
            data.forEach((d, i) => {
                const val = getVal(d, key);
                if (val === -1) return; // Không vẽ điểm và nhãn cho điểm bị Tràn RAM
                
                const x = paddingLeft + (i * (chartWidth / Math.max(1, data.length - 1)));
                const y = paddingTop + chartHeight - (val / maxMem) * chartHeight;
                
                if (data.length <= 50) {
                    ctx.beginPath();
                    ctx.arc(x, y, 4, 0, Math.PI * 2);
                    ctx.fill();
                }
                
                // Draw value label
                if (i % lblStep === 0 || i === data.length - 1) {
                    ctx.fillStyle = 'rgba(255, 255, 255, 0.7)';
                    ctx.font = '10px "DM Mono"';
                    ctx.textAlign = 'center';
                    
                    if (val === 0) {
                        ctx.textBaseline = 'bottom';
                        ctx.fillText('0 KB', x, y - 6);
                    } else if (key === 'listMemKb') {
                        // Draw label closer for List (lower point)
                        ctx.textBaseline = 'bottom';
                        ctx.fillText(formatYLabel(val), x, y - 6);
                    } else {
                        // Draw label higher up for Matrix (higher point)
                        ctx.textBaseline = 'bottom';
                        ctx.fillText(formatYLabel(val), x, y - 22);
                    }
                    
                    ctx.fillStyle = color; // reset for next point
                }
            });
        };

        drawLine('listMemKb', '#C9A96E');
        drawLine('matrixMemKb', '#E08F8F');
        
    }, [data, mode]);

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
                                {data.map((d, i) => {
                                    let listVal = mode === 'theory' ? d.listMemKb : d.actualListMemKb;
                                    const matrixVal = mode === 'theory' ? d.matrixMemKb : d.actualMatrixMemKb;
                                    let isGcFallback = false;
                                    
                                    if (mode === 'actual' && listVal === 0) {
                                        listVal = d.listMemKb;
                                        isGcFallback = true;
                                    }
                                    
                                    const formatMem = (val, theoryVal, isFallback) => {
                                        if (val === -1) {
                                            const neededMB = formatNum(Math.round(theoryVal / 1024));
                                            return <span style={{ color: '#ff4d4f', fontSize: '0.85rem', fontWeight: 'bold', background: 'rgba(255, 77, 79, 0.1)', padding: '2px 6px', borderRadius: '4px' }}>Tràn RAM (Cần ~{neededMB} MB)</span>;
                                        }
                                        const displayVal = formatNum(Math.round(val)) + ' KB';
                                        if (isFallback) {
                                            return <span title="Được khôi phục từ số liệu lý thuyết do Java GC dọn rác" style={{ color: '#C9A96E' }}>{displayVal} <span style={{fontSize:'10px', color:'#888'}}>*</span></span>;
                                        }
                                        return displayVal;
                                    };

                                    return (
                                        <tr key={i}>
                                            <td>{formatNum(d.size)}</td>
                                            <td>{formatNum(d.edges)}</td>
                                            <td style={{ color: '#C9A96E' }}>
                                                {loading ? <span style={{ color: '#888', fontStyle: 'italic', animation: 'pulse 1.5s infinite' }}>Đang đo...</span> : formatMem(listVal, d.listMemKb, isGcFallback)}
                                            </td>
                                            <td style={{ color: '#E08F8F' }}>
                                                {loading ? <span style={{ color: '#888', fontStyle: 'italic', animation: 'pulse 1.5s infinite' }}>Đang đo...</span> : formatMem(matrixVal, d.matrixMemKb, false)}
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>

                    <div>
                        <div className="chart-container" style={{ padding: '16px' }}>
                            <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', justifyContent: 'center', fontSize: '0.8rem', fontFamily: "'DM Mono', monospace" }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><div style={{ width:'12px', height:'2px', background:'#C9A96E' }}></div> Danh sách kề (KB)</div>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><div style={{ width:'12px', height:'2px', background:'#E08F8F' }}></div> Ma trận kề (KB)</div>
                            </div>
                            <div style={{ height: '300px', width: '100%' }}>
                                <canvas ref={canvasRef} style={{ width: '100%', height: '100%', display: 'block' }}></canvas>
                            </div>
                        </div>
                    </div>
                </div>

                <div style={{ marginTop: '5rem', marginBottom: '4rem' }}>
                    <h4 style={{ color: 'var(--gold)', marginBottom: '2rem', fontFamily: "'Cormorant Garamond', serif", fontStyle: 'italic', fontSize: '1.8rem', textAlign: 'center' }}>
                        Phân rã chi tiết cấu trúc cấp phát RAM (Java Core)
                    </h4>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem' }}>
                        
                        {/* Card 1: Adjacency List */}
                        <div style={{ background: 'var(--surface)', border: '1px solid rgba(201, 169, 110, 0.3)', borderRadius: '16px', padding: '24px' }}>
                            <h5 style={{ color: '#C9A96E', marginBottom: '1.5rem', fontFamily: "'DM Mono', monospace", fontSize: '1.1rem', textAlign: 'center', textTransform: 'uppercase', letterSpacing: '1px' }}>
                                1. Danh sách kề
                            </h5>
                            <div style={{ padding: '16px', background: 'rgba(201, 169, 110, 0.05)', borderRadius: '12px', marginBottom: '2rem', fontFamily: "'DM Mono', monospace", color: '#C9A96E', textAlign: 'center', fontSize: '1.2rem', border: '1px dashed rgba(201, 169, 110, 0.4)' }}>
                                V × 80 + E × 48 (Bytes)
                            </div>
                            
                            <div style={{ marginBottom: '1.5rem' }}>
                                <strong style={{ color: '#fff', fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    <div style={{width:'8px', height:'8px', borderRadius:'50%', background:'#C9A96E'}}></div> Đỉnh (Vertex) — Tốn 80 Bytes/Đỉnh
                                </strong>
                                <ul style={{ margin: '12px 0 0 0', paddingLeft: '1.5rem', color: 'var(--muted)', fontSize: '0.9rem', lineHeight: '1.6' }}>
                                    <li><strong>16 Bytes:</strong> Object Header cơ bản của Java (JVM).</li>
                                    <li><strong>32 Bytes:</strong> Cấu trúc Node lưu trữ trên Hash Map (chứa mã băm, con trỏ key, value).</li>
                                    <li><strong>32 Bytes:</strong> Bản thân Object đỉnh chứa các tham chiếu (reference) tới chuỗi ID và Danh sách liên kết.</li>
                                </ul>
                            </div>
                            
                            <div>
                                <strong style={{ color: '#fff', fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    <div style={{width:'8px', height:'8px', borderRadius:'50%', background:'#C9A96E'}}></div> Cạnh (Edge) — Tốn 48 Bytes/Cạnh
                                </strong>
                                <ul style={{ margin: '12px 0 0 0', paddingLeft: '1.5rem', color: 'var(--muted)', fontSize: '0.9rem', lineHeight: '1.6' }}>
                                    <li><strong>16 Bytes:</strong> Object Header của Node danh sách liên kết.</li>
                                    <li><strong>16 Bytes:</strong> Chứa thông tin trọng số (Weight/Double) và Data.</li>
                                    <li><strong>16 Bytes:</strong> Các con trỏ (Reference) trỏ đến đỉnh kế tiếp hoặc Node tiếp theo.</li>
                                </ul>
                            </div>
                        </div>

                        {/* Card 2: Adjacency Matrix */}
                        <div style={{ background: 'var(--surface)', border: '1px solid rgba(224, 143, 143, 0.3)', borderRadius: '16px', padding: '24px' }}>
                            <h5 style={{ color: '#E08F8F', marginBottom: '1.5rem', fontFamily: "'DM Mono', monospace", fontSize: '1.1rem', textAlign: 'center', textTransform: 'uppercase', letterSpacing: '1px' }}>
                                2. Ma trận kề
                            </h5>
                            <div style={{ padding: '16px', background: 'rgba(224, 143, 143, 0.05)', borderRadius: '12px', marginBottom: '2rem', fontFamily: "'DM Mono', monospace", color: '#E08F8F', textAlign: 'center', fontSize: '1.2rem', border: '1px dashed rgba(224, 143, 143, 0.4)' }}>
                                V<sup>2</sup> + V × 32 + 24 (Bytes)
                            </div>
                            
                            <div style={{ marginBottom: '1.5rem' }}>
                                <strong style={{ color: '#fff', fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    <div style={{width:'8px', height:'8px', borderRadius:'50%', background:'#E08F8F'}}></div> Dữ liệu gốc (Raw Data) — Tốn V² Bytes
                                </strong>
                                <ul style={{ margin: '12px 0 0 0', paddingLeft: '1.5rem', color: 'var(--muted)', fontSize: '0.9rem', lineHeight: '1.6' }}>
                                    <li>Kích thước mảng boolean 2 chiều thực tế (<code>boolean[][]</code>).</li>
                                    <li>Trong Java, mỗi phần tử boolean trong mảng tiêu tốn đúng <strong>1 byte</strong> bộ nhớ. Với mảng VxV, tốn đúng V² bytes.</li>
                                </ul>
                            </div>
                            
                            <div>
                                <strong style={{ color: '#fff', fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    <div style={{width:'8px', height:'8px', borderRadius:'50%', background:'#E08F8F'}}></div> Chi phí ngầm (Overhead) — Tốn V × 32 + 24 Bytes
                                </strong>
                                <ul style={{ margin: '12px 0 0 0', paddingLeft: '1.5rem', color: 'var(--muted)', fontSize: '0.9rem', lineHeight: '1.6' }}>
                                    <li><strong>V × 32 Bytes:</strong> Mảng 2 chiều thực chất là "một mảng chứa các mảng con". Hệ thống phải tạo ra V mảng con (mỗi mảng con đại diện cho 1 hàng ngang). Mỗi hàng con này tự tốn 24 bytes (16 bytes Header + 4 bytes Length + padding) cộng với 8 bytes con trỏ lưu tại mảng chính.</li>
                                    <li><strong>24 Bytes:</strong> Object Header và Length của mảng trục chính bao ngoài cùng.</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="conclusion-box">
                    <strong style={{ color: 'var(--gold)', display: 'block', marginBottom: '8px' }}>Kết luận (Conclusion)</strong>
                    <p style={{ margin: '0 0 8px 0', color: 'var(--muted)' }}>
                        Dựa trên kết quả đo lường, cấu trúc <strong>Adjacency List (Danh sách kề)</strong> tiết kiệm bộ nhớ hơn đáng kể so với <strong>Adjacency Matrix (Ma trận kề)</strong> khi ứng dụng vào mạng xã hội (vốn là đồ thị thưa - Sparse Graph).
                    </p>
                    <p style={{ margin: 0, color: 'var(--muted)' }}>
                        Đặc biệt khi số lượng đỉnh (Node) tăng lên mức hàng ngàn hoặc hàng triệu, dung lượng của Ma trận kề tăng theo hàm bậc hai <i style={{color: 'var(--gold)'}}>O(V<sup>2</sup>)</i>, trong khi Danh sách kề chỉ tăng tuyến tính <i style={{color: 'var(--gold)'}}>O(V + E)</i>, giúp ngăn chặn triệt để tình trạng tràn bộ nhớ (Out of Memory) trên hệ thống.
                    </p>
                </div>
            </div>
        </Layout>
    );
}
