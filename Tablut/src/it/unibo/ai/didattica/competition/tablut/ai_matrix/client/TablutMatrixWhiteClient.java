package it.unibo.ai.didattica.competition.tablut.ai_matrix.client;

import java.io.IOException;
import java.net.UnknownHostException;

import it.unibo.ai.didattica.competition.tablut.client.TablutRandomClient;

public class TablutMatrixWhiteClient {
    public static void main(String[] args) throws UnknownHostException, ClassNotFoundException, IOException {
		String[] array = new String[]{"WHITE"};
		if (args.length>0){
			array = new String[]{"WHITE", args[0]};
		}
		TablutRandomClient.main(array);
	}
}
