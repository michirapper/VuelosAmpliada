import java.util.Arrays;
import java.util.Random;

import org.bson.BasicBSONObject;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;


public class FuncionesMongo {
	static MongoClient mongo = crearConexion();

	public static MongoClient crearConexion() {
		MongoClient mongo = null;
		try {
			mongo = new MongoClient("localhost", 27017);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mongo;
	}

	protected static void verVuelos(MongoClient mongo) {
		DB db = mongo.getDB("VuelosAmpliada");
		DBCollection colleccionVuelos = db.getCollection("vuelo");
		DBCursor cur = colleccionVuelos.find();

		while (cur.hasNext()) {
			DBObject obj = cur.next();
			String linea = "Codigo: " + obj.get("codigo") + " -Origen: " + obj.get("origen") + " -Destino: "
					+ obj.get("destino") + " -Fecha: " + obj.get("fecha") + " -Hora: " + obj.get("hora")
					+ " -Plazas totales: " + obj.get("plazas_totales") + " -Plazas disponibles: "
					+ obj.get("plazas_disponibles");
			System.out.println(linea);
		}
	}

	protected static void comprarVuelo(String vuelo, String dniPasajero, String nombre, String apellido,
			String dniPagador, String tarjeta) {
		String codigoVenta = doCodigoVenta();
		String plazas_disponibles = buscar(vuelo);
		int PlazasD = Integer.parseInt(plazas_disponibles) - 1;

		MongoDatabase db = mongo.getDatabase("VuelosAmpliada");
		MongoCollection colleccionVuelos = db.getCollection("vuelo");

		Document quienCambio = new Document("codigo", vuelo);
		Document cambios = new Document("plazas_disponibles", PlazasD);
		Document auxSet = new Document("$set", cambios);
		colleccionVuelos.updateOne(quienCambio, auxSet);
		System.out.println(PlazasD);
		

	}

	protected static String doCodigoVenta() {
		char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
		StringBuilder sb = new StringBuilder(10);
		Random random = new Random();
		for (int i = 0; i < 9; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		String output = sb.toString().toUpperCase();
		return output;
	}

	protected static String buscar(String Codvuelo) {
		Document findDocument = new Document("codigo", Codvuelo);
		DB db = mongo.getDB("VuelosAmpliada");
		DBCollection colleccionVuelos = db.getCollection("vuelo");
		DBCursor cur = colleccionVuelos.find(new BasicDBObject("codigo", Codvuelo));

		while (cur.hasNext()) {
			Codvuelo = cur.next().get("plazas_disponibles").toString();
		}
		return Codvuelo;

	}

}
