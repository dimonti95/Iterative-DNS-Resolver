import org.xbill.DNS.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class MyDig {
    public static void main(String args[]) throws IOException {

        if(args.length < 1) {
            System.out.println("Invalid no. of arguments");
            System.exit(1);
        }

        /*
        for (String arg : args) {
            System.out.println("Arg 1: " + arg);
        }
        */

        long startTime = 0;
        long endTime   = 0;
        int  ans       = 0;

        Date date =  new Date();

        Name    name      = null;
        Message query     = null;
        Message response  = null;
        String  server    = null;
        Record  record    = null;
        Record[] question = null;
        Record[] answer   = null;

        SimpleResolver resolver = null;

        ArrayList<String> list = new ArrayList<>();

        /* Getting domain name from string input */
        name = Name.fromString(args[0], Name.root);

        int root = 1;
        int f = 0;
        int i = 0;
        while(true) {
            if(root == 1) {
                    server = "198.41.0.4"; // VeriSign, Inc. root server
                    root = 0;
            }
            else if(root == 0 && i < list.size()) {
                server = list.get(i++);
            } else {
                root = 1;
                continue;
            }

            //System.out.println("Selected Server: " + server);

            if (server != null) {
                resolver = new SimpleResolver(server);
            } else {
                resolver = new SimpleResolver();
            }

            startTime = System.currentTimeMillis();
            record    = Record.newRecord(name, Type.A, DClass.IN);

            /* Creating new message to send as a query */
            query = Message.newQuery(record);
            //System.out.println("QUERY:\n");
            //System.out.println(query);

            /* Sending message to server */
            response = resolver.send(query);
            //System.out.println("RESPONSE:\n");
            //System.out.println(response);

            if(response == null)
                continue;

            String header = response.getHeader().toString();

            if(header.contains("status: NOERROR")) {
                if(f == 0)
                {
                    question = response.getSectionArray(Section.QUESTION);
                }
                Record [] answerResponse = response.getSectionArray(Section.ANSWER);
                Record [] recordsResponse = response.getSectionArray(Section.AUTHORITY);
                if(answerResponse.length == 0) {
                    // no ans
                    if(recordsResponse.length != 0) {
                        list.clear();
                        i = 0;
                        for (Record aRecordsResponse : recordsResponse) {
                            list.add(aRecordsResponse.rdataToString());
                        }
                    }
                    else {
                        root = 1;
                    }
                }
                else {
                    // ans received
                    if(ans == 0)
                        ans = answerResponse.length;
                    for (Record anAnswerResponse : answerResponse) {
                        answer = answerResponse;
                        if (anAnswerResponse.getType() == Type.A) {
                            System.out.println("QUESTION SECTION:");
                            System.out.println(question[0].toString() + "\n");

                            System.out.println("ANSWER SECTION:");
                            for (int z = 0; z < ans; z++) {
                                System.out.println(answer[z].toString());
                            }
                            System.out.println(); // new line

                            endTime = System.currentTimeMillis();

                            System.out.print("Query Time: ");
                            System.out.print(endTime - startTime + " msec\n");

                            System.out.print("WHEN: ");
                            System.out.print(date + "\n");

                            System.out.println("MSG SIZE rcvd: " + response.numBytes());
                            return;
                        } else {
                            if (f == 0) {
                                f++;
                            }
                            name = Name.fromString(anAnswerResponse.rdataToString(), Name.root);
                            root = 1;
                        }
                    }
                }
            }
        }
    }
}
