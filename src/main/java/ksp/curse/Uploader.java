package ksp.curse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.StringParser;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.defaultsources.PropertyDefaultSource;
import com.martiansoftware.jsap.stringparsers.FileStringParser;

public class Uploader {
	public enum ReleaseType {
		RELEASE, BETA, ALPHA
	}

	private final HttpClient httpclient;

	public Uploader() {
		this(new DefaultHttpClient());
	}

	public Uploader(HttpClient client) {
		this.httpclient = client;
	}

	public JSONArray getModVersions(String game, String apiKey)
			throws IOException {
		HttpGet getVersions = new HttpGet("http://" + game
				+ ".curseforge.com/api/game/versions");
		getVersions.addHeader("X-Api-Token", apiKey);
		HttpResponse versionResponse = httpclient.execute(getVersions);
		JSONArray json;
		String body = null;
		try (Scanner scanner = new Scanner(versionResponse.getEntity()
				.getContent())) {
			body = scanner.useDelimiter("\\A").next();
			json = new JSONArray(body);
		} catch (JSONException ex) {
			throw new IOException("Parse error: " + body);
		}
		return json;
	}

	public long uploadMod(String game, String apiKey, long modId, File file,
			String changelog, ReleaseType releaseType, String... gameVersions)
			throws IOException {
		List<Long> versions = new ArrayList<>();
		JSONArray actualVersions = getModVersions(game, apiKey);
		for (String gameVersion : gameVersions) {
			boolean matched = false;
			for (int i = 0; i < actualVersions.length(); i++) {
				JSONObject serverVersion = actualVersions.getJSONObject(i);
				if (gameVersion.equals(serverVersion.get("id"))
						|| gameVersion.equals(serverVersion.get("name"))
						|| gameVersion.equals(serverVersion.get("slug"))) {
					versions.add(serverVersion.getLong("id"));
					matched = true;
					break;
				}
			}

			if (!matched) {
				StringBuilder validVersions = new StringBuilder();
				for (int i = 0; i < actualVersions.length(); i++) {
					if (i != 0) {
						validVersions.append(",");
					}
					validVersions.append(actualVersions.getJSONObject(i).get(
							"name"));
				}

				throw new IOException("Invalid game version: " + gameVersion
						+ ". (Valid versions: " + validVersions.toString()
						+ ")");
			}
		}

		HttpPost httpPost = new HttpPost("http://" + game
				+ ".curseforge.com/api/projects/" + modId + "/upload-file");
		httpPost.addHeader("X-Api-Token", apiKey);

		JSONObject metadata = new JSONObject();
		if (changelog == null)
			changelog = "";
		metadata.put("changelog", changelog);
		metadata.put("releaseType", releaseType.toString().toLowerCase());
		metadata.put("gameVersions", versions);

		FileBody uploadFilePart = new FileBody(file);
		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("file", uploadFilePart);
		reqEntity.addPart("metadata", new StringBody(metadata.toString()));
		httpPost.setEntity(reqEntity);

		HttpResponse response = httpclient.execute(httpPost);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IOException("Got status code "
					+ response.getStatusLine().getStatusCode());
		}
		JSONObject json;
		String body = null;
		try (Scanner scanner = new Scanner(response.getEntity().getContent())) {
			json = new JSONObject(scanner.useDelimiter("\\A").next());
		} catch (JSONException ex) {
			throw new IOException("Parse error: " + body);
		}

		return json.getLong("id");
	}

	public static class FileStringStringParser extends StringParser {
		@Override
		public Object parse(String arg) throws ParseException {
			if (arg.startsWith("@")) {
				File file = new File(arg.substring(1));
				if (file.exists()) {
					try (FileInputStream fis = new FileInputStream(file);
							Scanner scanner = new Scanner(fis)) {
						return scanner.useDelimiter("\\A").next();
					} catch (IOException e) {
						throw new ParseException(e);
					}
				} else {
					throw new ParseException("Argument " + arg
							+ " tried to load file "
							+ file.getAbsolutePath().toString()
							+ " but it does not exist!");
				}
			}
			return arg;
		}
	}

	public static void main(String[] args) throws IOException, JSAPException {
		FileStringStringParser parser = new FileStringStringParser();
		SimpleJSAP jsap = new SimpleJSAP(
				"CurseUploader",
				"Uploads build artifacts to Curseforge",
				new Parameter[] {
						new FlaggedOption("game").setStringParser(parser)
								.setDefault("kerbal").setRequired(true)
								.setShortFlag('g').setLongFlag("game")
								.setHelp("The Curseforge site to use"),
						new FlaggedOption("key")
								.setStringParser(parser)
								.setRequired(true)
								.setShortFlag('k')
								.setLongFlag("key")
								.setHelp(
										"Your Curseforge API key. Can be obtained at http://kerbal.curseforge.com/my-api-tokens"),
						new FlaggedOption("mod")
								.setStringParser(parser)
								.setRequired(true)
								.setShortFlag('m')
								.setLongFlag("mod")
								.setHelp(
										"The ID of your mod on Curseforge. Can be found in the URL: http://kerbal.curseforge.com/ksp-mods/MOD_ID_HERE-MOD_NAME_HERE"),
						new FlaggedOption("changelog").setStringParser(parser)
								.setRequired(false).setShortFlag('c')
								.setLongFlag("changelog")
								.setHelp("Changelog text for this release"),
						new FlaggedOption("release").setStringParser(parser)
								.setDefault("release").setRequired(true)
								.setShortFlag('t').setLongFlag("type")
								.setHelp("Valid values: release,beta,alpha"),
						new FlaggedOption("version")
								.setStringParser(parser)
								.setRequired(true)
								.setShortFlag('v')
								.setLongFlag("version")
								.setList(true)
								.setListSeparator(',')
								.setHelp(
										"Versions of the game that your mod supports (example: 0.23.5,0.23)"),
						new UnflaggedOption("file")
								.setStringParser(FileStringParser.getParser())
								.setRequired(true)
								.setHelp("The file to release to Curseforge") });
		jsap.registerDefaultSource(new PropertyDefaultSource(System
				.getProperty("user.home") + "/curse.conf", false));
		jsap.registerDefaultSource(new PropertyDefaultSource("curse.conf",
				false));

		JSAPResult config = jsap.parse(args);

		if (jsap.messagePrinted()) {
			if (!config.getBoolean("help")) {
				System.err.println();
				System.err.println("Use --help for more information");
			}
			System.exit(1);
		}

		ReleaseType releaseType = null;
		for (ReleaseType type : ReleaseType.values()) {
			if (type.toString().equalsIgnoreCase(config.getString("release"))) {
				releaseType = type;
				break;
			}
		}
		if (releaseType == null) {
			System.err.println("Unknown releaseType: "
					+ config.getString("releaseType")
					+ ". Valid values: release,beta,alpha");
			System.exit(1);
			return;
		}

		File file = config.getFile("file");
		if (file == null || !file.exists()) {
			System.err
					.println("Could not find file: " + file.getAbsolutePath());
			System.exit(1);
			return;
		}

		Long mod;
		try {
			mod = Long.parseLong(config.getString("mod"));
		} catch (NumberFormatException ex) {
			System.err.println(config.getString("mod")
					+ " is not a valid mod ID. It must be a number.");
			System.exit(1);
			return;
		}

		Uploader uploader = new Uploader();
		long fileid = uploader.uploadMod(config.getString("game"),
				config.getString("key"), mod, file,
				config.getString("changelog"), releaseType,
				config.getStringArray("version"));
		System.out.println(fileid);
	}
}
