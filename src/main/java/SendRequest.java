import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;

public class SendRequest {

    public static final String PAYLOAD =
                    "{\"statements\": [{\"resultDataContents\": [\"row\",\"graph\"],\"statement\": "
                                    + "\"CREATE (matrix1:Movie { title : 'The Matrix', year : '1999-03-31' })"
                                    + "CREATE (matrix2:Movie { title : 'The Matrix Reloaded', year : '2003-05-07' })"
                                    + "CREATE (matrix3:Movie { title : 'The Matrix Revolutions', year : '2003-10-27' })"
                                    + "CREATE (johnwick:Movie {title : 'John Wick'})"
                                    + "CREATE (keanu:Actor { name:'Keanu Reeves' })"
                                    + "CREATE (william:Actor { name:'William Dafoe' })"
                                    + "CREATE (mike:Actor { name:'Michael Nyquist' })"
                                    + "CREATE (laurence:Actor { name:'Laurence Fishburne' })"
                                    + "CREATE (carrieanne:Actor { name:'Carrie-Anne Moss' })"
                                    + "CREATE (chad:Director { name:'Chad Stahelski' })"
                                    + "CREATE (david:Director { name:'David Leitch' })"
                                    + "CREATE (keanu)-[:ACTS_IN { role : 'Neo' }]->(matrix1)"
                                    + "CREATE (keanu)-[:ACTS_IN { role : 'Neo' }]->(matrix2)"
                                    + "CREATE (keanu)-[:ACTS_IN { role : 'Neo' }]->(matrix3)"
                                    + "CREATE (keanu)-[:ACTS_IN { role : 'John' }]->(johnwick)"
                                    + "CREATE (william)-[:ACTS_IN { role : 'Marcus' }]->(johnwick)"
                                    + "CREATE (mike)-[:ACTS_IN { role : 'Viggo' }]->(johnwick)"
                                    + "CREATE (chad)-[:DIRECTED_IN { role : 'chad' }]->(johnwick)"
                                    + "CREATE (david)-[:DIRECTED_IN { role : 'david' }]->(johnwick)"
                                    + "CREATE (laurence)-[:ACTS_IN { role : 'Morpheus' }]->(matrix1)"
                                    + "CREATE (laurence)-[:ACTS_IN { role : 'Morpheus' }]->(matrix2)"
                                    + "CREATE (laurence)-[:ACTS_IN { role : 'Morpheus' }]->(matrix3)"
                                    + "CREATE (carrieanne)-[:ACTS_IN { role : 'Trinity' }]->(matrix1)"
                                    + "CREATE (carrieanne)-[:ACTS_IN { role : 'Trinity' }]->(matrix2)"
                                    + "CREATE (carrieanne)-[:ACTS_IN { role : 'Trinity' }]->"
                                    + "(matrix3)\",\"includeStats\": true}]}";

    public static void main(String[] args) {

        sendRequest();
    }

    private static void sendRequest() {

        // Open Transaction NEO4J (POST )

        try {
            // Create request
            Content content = Request.Post("http://localhost:7474/db/data/transaction")

                                     // Add headers
                                     .addHeader("X-stream", "true")
                                     .addHeader("Authorization", "Basic bmVvNGo6bWFuaTE0ODY=")
                                     .addHeader("Content-Type", "application/json")
                                     .addHeader("Accept", "application/json")

                                     // Add body
                                     .bodyString(PAYLOAD, ContentType.APPLICATION_JSON)

                                     // Fetch request and return content
                                     .execute().returnContent();

            // Print content
            System.out.println(content);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

