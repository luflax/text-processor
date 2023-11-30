import { scaleOrdinal, schemeCategory10, drag, create, forceCenter, forceManyBody, forceLink, forceSimulation, select, forceRadial, forceCollide, zoom, zoomIdentity, interpolateRound, forceY } from 'd3';

export class GraphRender {
    public static loadGraph(nativeElement: any, data: any) {
        const color = scaleOrdinal(schemeCategory10);
        const color2 = scaleOrdinal(schemeCategory10);

        const links = data.links.map((d: any) => ({ ...d }));
        const nodes = data.nodes.map((d: any) => ({ ...d }));

        const simulation = forceSimulation(nodes)
            .force("link", forceLink(links).id((d: any) => d.id).distance(200).strength(1))
            .force("charge", forceManyBody().strength(100))
            .force("collide", forceCollide().radius(d => 20 + 120))
            .on("tick", ticked);

        const svg = select(nativeElement);

        svg.selectAll("*").remove();

        const g = svg.append("g");

        const zoomO = zoom().on("zoom", (transform) => g.attr("transform", transform.transform));

        svg.call(zoomO).call(zoomO.transform, zoomIdentity);

        const link = g.append("g")
            .selectAll()
            .data(links)
            .join("g");


        const linkLine = link.append("line")
            .attr("stroke", "#FFF")
            .attr("stroke-opacity", 1)

        const linkText = link.append("text")
            .attr("fill", (d: any) => color2(d.value))
            .attr("font-weight", "bold")
            .attr("font-size", "32")
            .text((d: any) => d.value);

        const node = g.append("g")
            .selectAll()
            .data(nodes)
            .join("g");

        const nodeCircle = node.append("circle")
            .attr("stroke", (d: any) => color(d.group))
            .attr("stroke-width", 1.5)
            .attr("r", 25)
            .attr("fill", (d: any) => color(d.group));

        const nodeText = node.append("text")
            .attr("fill", "#fff")
            .attr("font-weight", "bold")
            .attr("text-anchor", "middle")
            .attr("font-size", "32")
            .text((d: any) => d.id);

        node.call(drag()
            .on("start", dragstarted)
            .on("drag", dragged)
            .on("end", dragended) as any);

        function ticked() {
            linkLine
                .attr("x1", (d: any) => d.source.x)
                .attr("y1", (d: any) => d.source.y)
                .attr("x2", (d: any) => d.target.x)
                .attr("y2", (d: any) => d.target.y);

            linkText
                .attr("x", (d: any) => d.source.x + ((d.target.x - d.source.x) / 2))
                .attr("y", (d: any) => d.source.y + ((d.target.y - d.source.y) / 2));

            nodeText
                .attr("x", (d: any) => d.x)
                .attr("y", (d: any) => d.y);

            nodeCircle
                .attr("cx", (d: any) => d.x)
                .attr("cy", (d: any) => d.y);
        }

        function dragstarted(event: { active: any; subject: { fx: any; x: any; fy: any; y: any; }; }) {
            if (!event.active) simulation.alphaTarget(0.5).restart();
            event.subject.fx = event.subject.x;
            event.subject.fy = event.subject.y;
        }

        function dragged(event: { subject: { fx: any; fy: any; }; x: any; y: any; }) {
            event.subject.fx = event.x;
            event.subject.fy = event.y;
        }

        function dragended(event: { active: any; subject: { fx: null; fy: null; }; }) {
            if (!event.active) simulation.alphaTarget(0);
            event.subject.fx = null;
            event.subject.fy = null;
        }

        return svg.node();
    }
}