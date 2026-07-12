import * as d3 from 'd3';

export function renderNetworkGraph(containerId, data, onNodeClick = null, currentUserId = null) {
    const container = document.getElementById(containerId);
    if (!container || !data || !data.nodes || !data.links) return;
    
    container.innerHTML = '';
    
    const width = container.clientWidth;
    const height = container.clientHeight;
    
    const svg = d3.select(`#${containerId}`)
        .append('svg')
        .attr('width', width)
        .attr('height', height)
        .call(d3.zoom().on('zoom', (event) => {
            g.attr('transform', event.transform);
        }));
        
    const g = svg.append('g');
    
    const simulation = d3.forceSimulation(data.nodes)
        .force('link', d3.forceLink(data.links).id(d => d.id).distance(100))
        .force('charge', d3.forceManyBody().strength(-300))
        .force('center', d3.forceCenter(width / 2, height / 2))
        .force('collide', d3.forceCollide().radius(35));
        
    const link = g.append('g')
        .attr('stroke', 'rgba(255,255,255,0.1)')
        .attr('stroke-opacity', 0.6)
        .selectAll('line')
        .data(data.links)
        .join('line')
        .attr('stroke-width', 1);
        
    const node = g.append('g')
        .selectAll('g')
        .data(data.nodes)
        .join('g')
        .attr('cursor', 'pointer')
        .call(drag(simulation))
        .on('click', (event, d) => {
            if (onNodeClick) onNodeClick(d);
        });
        
    node.append('circle')
        .attr('r', d => d.id === currentUserId ? 28 : 20)
        .attr('fill', d => d.id === currentUserId ? '#C9A96E' : '#141824')
        .attr('stroke', '#C9A96E')
        .attr('stroke-width', d => d.id === currentUserId ? 3 : 2)
        .style('filter', d => d.id === currentUserId ? 'drop-shadow(0 0 10px rgba(201, 169, 110, 0.6))' : 'none');
        
    node.append('text')
        .text(d => d.avatar || d.name.substring(0, 2).toUpperCase())
        .attr('text-anchor', 'middle')
        .attr('dy', '0.35em')
        .attr('fill', d => d.id === currentUserId ? '#141824' : '#C9A96E')
        .attr('font-size', d => d.id === currentUserId ? '16px' : '12px')
        .attr('font-family', 'monospace')
        .attr('font-weight', d => d.id === currentUserId ? 'bold' : 'normal');
        
    node.append('text')
        .text(d => d.name)
        .attr('dy', d => d.id === currentUserId ? 45 : 35)
        .attr('text-anchor', 'middle')
        .attr('fill', d => d.id === currentUserId ? '#C9A96E' : '#E8E4DC')
        .attr('font-size', d => d.id === currentUserId ? '14px' : '12px')
        .attr('font-family', 'sans-serif')
        .attr('font-weight', d => d.id === currentUserId ? 'bold' : 'normal');
        
    simulation.on('tick', () => {
        link
            .attr('x1', d => d.source.x)
            .attr('y1', d => d.source.y)
            .attr('x2', d => d.target.x)
            .attr('y2', d => d.target.y);
            
        node
            .attr('transform', d => `translate(${d.x},${d.y})`);
    });
    
    function drag(simulation) {
        function dragstarted(event) {
            if (!event.active) simulation.alphaTarget(0.3).restart();
            event.subject.fx = event.subject.x;
            event.subject.fy = event.subject.y;
        }
        
        function dragged(event) {
            event.subject.fx = event.x;
            event.subject.fy = event.y;
        }
        
        function dragended(event) {
            if (!event.active) simulation.alphaTarget(0);
            event.subject.fx = null;
            event.subject.fy = null;
        }
        
        return d3.drag()
            .on('start', dragstarted)
            .on('drag', dragged)
            .on('end', dragended);
    }
}
