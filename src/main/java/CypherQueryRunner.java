import com.google.gson.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by bhavanishekhawat
 *
 */
public class CypherQueryRunner {

    private static final String SERVER_ROOT_URI = "http://localhost:7474/db/data/";

    public static void main(String[] args) throws URISyntaxException {

        checkDatabaseIsRunning();
    }

    private static void checkDatabaseIsRunning() {

        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("neo4j", "mani1486")); // <-- that's it!
        WebResource resource = client.resource(SERVER_ROOT_URI);
        ClientResponse response = resource.get(ClientResponse.class);

        System.out.println(String.format("GET on [%s], status code [%d]", SERVER_ROOT_URI,
                        response.getStatus()));
        response.close();
    }

    private static Node sendTransactionalCypherQuery(String query) {

        TraversalDefinition t = new TraversalDefinition();
        t.setOrder(TraversalDefinition.DEPTH_FIRST);
        t.setUniqueness(TraversalDefinition.NODE);
        t.setMaxDepth(10);
        t.setReturnFilter(TraversalDefinition.ALL);
        t.setRelationships(new Relation("Actor", Relation.OUT));

        final String txUri = SERVER_ROOT_URI + "transaction/commit";
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("neo4j", "mani1486")); // <-- that's it!
        WebResource resource = client.resource(txUri);

        String payload = "{\"statements\" : [ {\"statement\" : \"" + query + "\"} ]}";
        ClientResponse response =
                        resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                                .entity(payload).post(ClientResponse.class);

        String json = response.getEntity(String.class);
        JsonObject jsonObject = (new JsonParser()).parse(json).getAsJsonObject();
        JsonArray id = jsonObject.getAsJsonArray("results");
        JsonElement node = id.get(0).getAsJsonObject().get("data").getAsJsonArray();
        JsonElement arr = node.getAsJsonArray().get(0);
        JsonElement object = arr.getAsJsonObject().get("row").getAsJsonArray();
        long actorId = object.getAsLong();
        response.close();

        System.out.println(String.format("POST [%s] to [%s], status code [%d], returned data: "
                                        + System.lineSeparator() + "%s", payload, txUri, response.getStatus(),
                        json));
        Node actorNode = attachToExistingNode(actorId);
        return actorNode;
    }

    private static URI createNode() {
        // START SNIPPET: createNode
        // final String txUri = SERVER_ROOT_URI + "transaction/commit";
        final String nodeEntryPointUri =
                        SERVER_ROOT_URI + "node"; // http://localhost:7474/db/data/node
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("neo4j", "mani1486")); // <-- that's it!
        WebResource resource = client.resource(nodeEntryPointUri);
        // POST {} to the node entry point URI
        ClientResponse response =
                        resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                                .entity("{}").post(ClientResponse.class);
        final URI location = response.getLocation();
        System.out.println(String.format("POST to [%s], status code [%d], location header [%s]",
                        nodeEntryPointUri, response.getStatus(), location.toString()));
        response.close();
        return location;
        // END SNIPPET: createNode
    }

    private static void addProperty(URI nodeUri, String propertyName, String propertyValue) {
        // START SNIPPET: addProp
        String propertyUri = nodeUri.toString() + "/properties/"
                        + propertyName; // http://localhost:7474/db/data/node/{node_id}/properties/{property_name}
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("neo4j", "mani1486")); // <-- that's it!
        WebResource resource = client.resource(propertyUri);
        ClientResponse response =
                        resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                                .entity("\"" + propertyValue + "\"").put(ClientResponse.class);
        System.out.println(String.format("PUT to [%s], status code [%d]", propertyUri,
                        response.getStatus()));
        response.close();
        // END SNIPPET: addProp
    }


    private static URI addRelationship(URI startNode, URI endNode, String relationshipType,
                                       String jsonAttributes) throws URISyntaxException {

        URI fromUri = new URI(startNode.toString() + "/relationships");
        String relationshipJson =
                        generateJsonRelationship(endNode, relationshipType, jsonAttributes);

        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("neo4j", "mani1486")); // <-- that's it!
        WebResource resource = client.resource(fromUri);
        // POST JSON to the relationships URI
        ClientResponse response =
                        resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                                .entity(relationshipJson).post(ClientResponse.class);

        final URI location = response.getLocation();
        System.out.println(String.format("POST to [%s], status code [%d], location header [%s]",
                        fromUri, response.getStatus(), location.toString()));

        response.close();
        return location;
    }

    private static String generateJsonRelationship(URI endNode, String relationshipType,
                                                   String... jsonAttributes) {

        StringBuilder sb = new StringBuilder();
        sb.append("{ \"to\" : \"");
        sb.append(endNode.toString());
        sb.append("\", ");

        sb.append("\"type\" : \"");
        sb.append(relationshipType);
        if (jsonAttributes == null || jsonAttributes.length < 1) {
            sb.append("\"");
        }
        else {
            sb.append("\", \"data\" : ");
            for (int i = 0; i < jsonAttributes.length; i++) {
                sb.append(jsonAttributes[i]);
                if (i < jsonAttributes.length - 1) { // Miss off the final comma
                    sb.append(", ");
                }
            }
        }

        sb.append(" }");
        return sb.toString();
    }

    public static Node attachToExistingNode(Long id) {

        GraphDatabaseService graphDatabaseService =
                        new GraphDatabaseFactory().newEmbeddedDatabase(SERVER_ROOT_URI);
        Transaction tx = graphDatabaseService.beginTx();
        Node node = graphDatabaseService.getNodeById(id);
        System.out.println("Node found: " + node);
        tx.close();
        return node;
    }

}
