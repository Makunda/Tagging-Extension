package com.castsoftware.tagging.models;

import com.castsoftware.tagging.config.Configuration;
import com.castsoftware.tagging.database.Neo4jAL;
import com.castsoftware.tagging.exceptions.neo4j.Neo4jBadRequest;
import com.castsoftware.tagging.exceptions.neo4j.Neo4jNoResult;
import com.castsoftware.tagging.exceptions.neo4j.Neo4jQueryException;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class TagRequestNode extends Neo4jObject{

    // Configuration properties
    private static final String LABEL = Configuration.get("neo4j.nodes.t_tag_node");
    private static final String NAME_PROPERTY = Configuration.get("neo4j.nodes.t_tag_node.name");
    private static final String REQUEST_PROPERTY = Configuration.get("neo4j.nodes.t_tag_node.request");
    private static final String ACTIVE_PROPERTY = Configuration.get("neo4j.nodes.t_tag_node.active");
    private static final String ERROR_PREFIX = Configuration.get("neo4j.nodes.t_tag_node.error_prefix");

    // Node properties
    private String name;
    private String tagRequest;
    private Boolean active;

    public static String getLabel() {
        return LABEL;
    }

    public static String getNameProperty() {
        return NAME_PROPERTY;
    }

    @Override
    protected Node findNode() throws Neo4jBadRequest, Neo4jNoResult {
        String initQuery = String.format("MATCH (n:%s) WHERE ID(n)=%d RETURN n as node LIMIT 1;", LABEL, this.getNodeId());
        try {
            Result res = neo4jAL.executeQuery(initQuery);
            Node n = (Node) res.next().get("node");
            this.setNode(n);

            return n;
        } catch (Neo4jQueryException e) {
            throw new Neo4jBadRequest(LABEL + " node initialization failed", initQuery , e, ERROR_PREFIX+"FIN1");
        } catch (NoSuchElementException |
                NullPointerException e) {
            throw new Neo4jNoResult(String.format("You need to create %s node first.", LABEL),  initQuery, e, ERROR_PREFIX+"FIN2");
        }
    }

    @Override
    public Node createNode() throws Neo4jBadRequest, Neo4jNoResult {
        String queryDomain = String.format("MERGE (p:%s { %s : '%s', %s : '%s', %s : %b }) RETURN p as node;",
                LABEL, NAME_PROPERTY, name, REQUEST_PROPERTY, tagRequest, ACTIVE_PROPERTY, this.active);
        try {
            Result res = neo4jAL.executeQuery(queryDomain);
            Node n = (Node) res.next().get("node");
            this.setNode(n);
            return n;
        } catch (Neo4jQueryException e) {
            throw new Neo4jBadRequest(LABEL + " node creation failed", queryDomain , e, ERROR_PREFIX+"CRN1");
        } catch (NoSuchElementException |
                NullPointerException e) {
            throw new Neo4jNoResult(LABEL + "node creation failed",  queryDomain, e, ERROR_PREFIX+"CRN2");
        }
    }

    public static List<TagRequestNode> getAllNodes(Neo4jAL neo4jAL) throws Neo4jBadRequest {
        try {
            List<TagRequestNode> resList = new ArrayList<>();
            ResourceIterator<Node> resIt = neo4jAL.findNodes(Label.label(LABEL));
            while ( resIt.hasNext() ) {
                try {
                    Node node = (Node) resIt.next();
                    String name = (String) node.getProperty(NAME_PROPERTY);
                    Boolean active = Boolean.parseBoolean((String) node.getProperty(ACTIVE_PROPERTY));
                    String request = (String) node.getProperty(REQUEST_PROPERTY);

                    // Initialize the node
                    TagRequestNode cn = new TagRequestNode(neo4jAL, name, active, request);
                    cn.setNode(node);

                    resList.add(cn);
                }  catch (NoSuchElementException |
                        NullPointerException e) {
                    throw new Neo4jNoResult(LABEL + " nodes retrieving failed",  "findQuery", e, ERROR_PREFIX+"GAN1");
                }
            }
            return resList;
        } catch (Neo4jQueryException | Neo4jNoResult e) {
            throw new Neo4jBadRequest(LABEL + " nodes retrieving failed", "findQuery" , e, ERROR_PREFIX+"GAN1");
        }
    }

    @Override
    public void deleteNode() throws Neo4jBadRequest {
        String queryDomain = String.format("MATCH (p:%s) WHERE ID(p)=%d DETACH DELETE p;",
                LABEL, this.getNodeId());
        try {
            neo4jAL.executeQuery(queryDomain);
        } catch (Neo4jQueryException e) {
            throw new Neo4jBadRequest(LABEL + " node deletion failed", queryDomain , e, ERROR_PREFIX+"DEL1");
        }
    }

    public TagRequestNode(Neo4jAL nal, String name, Boolean active, String tagRequest) {
        super(nal);
        this.name = name;
        this.active = active;
        this.tagRequest = tagRequest;
    }
}
