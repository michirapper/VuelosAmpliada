import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

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
import org.json.*;

public class FuncionesMongo {
	static MongoClient mongo = crearConexion();
	static ArrayList asientosOcupped = new ArrayList(200);
	static ArrayList asientosFree = new ArrayList(200);
	//static String asientoDeSiempre = "";

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
		if (Integer.parseInt(plazas_disponibles) == 0) {
			System.out.println("No hay asientos disponibles");
		}else {
			int PlazasD = Integer.parseInt(plazas_disponibles) - 1;
			int asiento = asientos(vuelo);

			MongoDatabase db = mongo.getDatabase("VuelosAmpliada");
			MongoCollection colleccionVuelos = db.getCollection("vuelo");

			Document quienCambio = new Document("codigo", vuelo);
			Document cambios = new Document("plazas_disponibles", PlazasD);
			Document auxSet = new Document("$set", cambios);
			colleccionVuelos.updateOne(quienCambio, auxSet);

			Document cambios2 = new Document("asiento", asiento).append("dni", dniPasajero).append("apellido", apellido)
					.append("nombre", nombre).append("dniPagador", dniPagador).append("tarjeta", tarjeta)
					.append("codigoVenta", codigoVenta);
			Document auxSet1 = new Document("vendidos", cambios2);
			Document auxSet2 = new Document("$push", auxSet1);
			colleccionVuelos.updateOne(quienCambio, auxSet2);
			System.out.println("Su codigo de venta es: " + codigoVenta);
		}
		

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
		String Pdisponibles = "";

		while (cur.hasNext()) {
			Pdisponibles = cur.next().get("plazas_disponibles").toString();
		}
		return Pdisponibles;

	}

	protected static int asientos(String vuelo) {
		// Document findDocument = new Document("codigo", "IB706");
		DB db = mongo.getDB("VuelosAmpliada");
		DBCollection colleccionVuelos = db.getCollection("vuelo");
		DBCursor cur = colleccionVuelos.find(new BasicDBObject("codigo", vuelo));
		String vendidos = "";

		while (cur.hasNext()) {
			vendidos = cur.next().get("vendidos").toString();

		}
		// System.out.println(vendidos);

		JSONArray jsonArray = new JSONArray(vendidos);
		CargarArray(jsonArray);

		for (int i = 1; i < 200; i++) {
			asientosFree.add(i);
		}
		// Compara Arrays y da los libres
		asientosFree.removeAll(asientosOcupped);

		Object[] asientoLibre = asientosFree.toArray();
		// System.out.println(asientoLibre[0]);
		int asiento = (Integer) asientoLibre[0];
		return asiento;

	}

	public static void CargarArray(JSONArray jsonArrayy) {
		for (int i = 0; i < jsonArrayy.length(); i++) {
			JSONObject json = jsonArrayy.getJSONObject(i);
			asientosOcupped.add(json.get("asiento"));
		}
	}

	protected static void borrarVuelo(String vuelo, String dniPasajero, String codigoVenta) {

		String plazas_disponibles = buscar(vuelo);
		int PlazasD = Integer.parseInt(plazas_disponibles) + 1;
		int asiento = asientos(vuelo);

		MongoDatabase db = mongo.getDatabase("VuelosAmpliada");
		MongoCollection colleccionVuelos = db.getCollection("vuelo");

		Document quienCambio = new Document("codigo", vuelo);
		Document cambios = new Document("plazas_disponibles", PlazasD);
		Document auxSet = new Document("$set", cambios);
		colleccionVuelos.updateOne(quienCambio, auxSet);

		Document cambios2 = new Document("dni", dniPasajero).append("codigoVenta", codigoVenta);
		Document auxSet1 = new Document("vendidos", cambios2);
		Document auxSet2 = new Document("$pull", auxSet1);
		colleccionVuelos.updateOne(quienCambio, auxSet2);
		System.out.println("Eliminar");

	}

	protected static void modificarVuelo(String vuelo, String dniPasajero, String codigoVenta) {

		MongoDatabase db = mongo.getDatabase("VuelosAmpliada");
		MongoCollection colleccionVuelos = db.getCollection("vuelo");
		int asiento = asientoDeSiempre(vuelo, dniPasajero);
		borrarVuelo(vuelo, dniPasajero, codigoVenta);

		Scanner sc = new Scanner(System.in);
		System.out.println("Datos a modificar: ");
		System.out.println("Apellido:");
		String apellido = sc.nextLine();
		System.out.println("Nombre:");
		String nombre = sc.nextLine();
		System.out.println("DNI de que ha comprado el vuelo:");
		String dniPagador = sc.nextLine();
		System.out.println("Tarjeta:");
		String tarjeta = sc.nextLine();
		System.out.println("DNI pasajero:");
		String dniPasajeroCambio = sc.nextLine();

		anadirActualizacion(vuelo, dniPasajeroCambio, nombre, apellido, dniPagador, tarjeta, asiento, codigoVenta);

		System.out.println("Modificado");

	}

	protected static void anadirActualizacion(String vuelo, String dniPasajero, String nombre, String apellido,
			String dniPagador, String tarjeta, int asiento, String codigoVenta) {

		//asiento = asientoDeSiempre;
		MongoDatabase db = mongo.getDatabase("VuelosAmpliada");
		MongoCollection colleccionVuelos = db.getCollection("vuelo");

		Document quienCambio = new Document("codigo", vuelo);

		Document cambios2 = new Document("asiento", asiento).append("dni", dniPasajero).append("apellido", apellido)
				.append("nombre", nombre).append("dniPagador", dniPagador).append("tarjeta", tarjeta)
				.append("codigoVenta", codigoVenta);
		Document auxSet1 = new Document("vendidos", cambios2);
		Document auxSet2 = new Document("$push", auxSet1);
		colleccionVuelos.updateOne(quienCambio, auxSet2);

	}

	protected static int asientoDeSiempre(String vuelo, String dni) {
		// Document findDocument = new Document("codigo", "IB706");
		DB db = mongo.getDB("VuelosAmpliada");
		DBCollection colleccionVuelos = db.getCollection("vuelo");
		DBCursor cur = colleccionVuelos.find(new BasicDBObject("codigo", vuelo));
		String vendidos = "";

		while (cur.hasNext()) {
			vendidos = cur.next().get("vendidos").toString();

		}
		// System.out.println(vendidos);

		JSONArray jsonArray = new JSONArray(vendidos);

		return cogerAsientoDeSiempre(jsonArray, dni);

	}

	public static int cogerAsientoDeSiempre(JSONArray jsonArrayy, String dni) {
		String asientoDeSiempre = "";
		for (int i = 0; i < jsonArrayy.length(); i++) {
			JSONObject json = jsonArrayy.getJSONObject(i);
			if (dni.equals(json.get("dni"))) {
				asientoDeSiempre = json.get("asiento").toString();
			}
		}
		
		return Integer.parseInt(asientoDeSiempre);
	}

}
