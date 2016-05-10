package com.adelcastillo.demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Contact;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.ContactLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author adelcastillo
 */
@Component(property = { "osgi.command.function=addUsers", "osgi.command.scope=blade" }, service = Object.class)
public class UserCountCommand {

	public UserLocalService getUserLocalService() {
		return _userLocalService;
	}

	@Reference
	public void setUserLocalService(UserLocalService _userLocalService) {
		this._userLocalService = _userLocalService;
	}

	public void addUsers(long companyId) {
		addUsers(1,companyId);
	}
	
	public void addUsers(int number, long companyId) {
		_log.info("Users before update: " + getUserLocalService().getUsersCount());
		for (int i = 0; i < number; i++) {
			createUser(companyId);
		}
		_log.info("Users after update: " + getUserLocalService().getUsersCount());
	}

	private JsonObject createUser(long companyId) {
		String sURL = "http://api.randomuser.me/?nat=gb,es"; // just a string
		JsonObject rootobj = null;
		// Connect to the URL using java's native library
		URL url;
		try {
			url = new URL(sURL);
			
			HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.connect();

			// Convert to a JSON object to print data
			JsonParser jp = new JsonParser(); // from gson
			JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
			rootobj = root.getAsJsonObject(); // May be an array, may be an
												// object.
			JsonElement results = rootobj.get("results");
			JsonArray array = results.getAsJsonArray();
			rootobj = array.get(0).getAsJsonObject();
			String gender = rootobj.get("gender").getAsString();
		    
		    String firstname = rootobj.get("name").getAsJsonObject().get("first").getAsString();
		    String lastname = rootobj.get("name").getAsJsonObject().get("last").getAsString();
		    firstname = StringUtil.upperCaseFirstLetter(firstname);
		    lastname = StringUtil.upperCaseFirstLetter(lastname);
		    String email =  rootobj.get("email").getAsString();
		    String username = rootobj.get("login").getAsJsonObject().get("username").getAsString();
		    String picture = rootobj.get("picture").getAsJsonObject().get("medium").getAsString();
		    
		    Contact newContact = ContactLocalServiceUtil.createContact(CounterLocalServiceUtil.increment(Contact.class.getName()));
		    boolean male = false;
		    if (gender.equals("male"))
				male = true;
		    long[] groupIds = {}; 
		    long[] organizationIds= {};
			long[] roleIds= {};
			long[] userGroupIds= {};
			ContactLocalServiceUtil.updateContact(newContact); 
		    User newUser = _userLocalService.addUser(
		    		CompanyLocalServiceUtil.getCompanyById(companyId).getDefaultUser().getUserId(),
		    		companyId, false,
		    		"test", "test", false,
					username, email, (long) 0, "",
					new Locale("en_US"), firstname, "",
					lastname, (long) 0, (long) 0, male,
					1, 1, 1980,
					"", groupIds, organizationIds,
					roleIds, userGroupIds, false,
					null);
			_log.info("Added user: ["+newUser.getUserId()+"] "+username+" ("+email+")");
			
			
			byte[] portraitBytes = downloadUrl(new URL(picture));
			_userLocalService.updatePortrait(newUser.getUserId(), portraitBytes);
			_log.info("Added Profile pic for: ["+newUser.getUserId()+"] "+username+" ("+email+")");
			

		} catch (MalformedURLException e) {
			_log.error("URL error> "+e);
		} catch (IOException e) {
			_log.error("Problem parsing URL> "+e);
		} catch (PortalException e) {
			_log.error("Portal problem>"+e);
		}
		return rootobj;
	}
	
	private byte[] downloadUrl(URL toDownload) {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    try {
	        byte[] chunk = new byte[4096];
	        int bytesRead;
	        InputStream stream = toDownload.openStream();

	        while ((bytesRead = stream.read(chunk)) > 0) {
	            outputStream.write(chunk, 0, bytesRead);
	        }

	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }

	    return outputStream.toByteArray();
	}

	private UserLocalService _userLocalService;
	
	private Log _log = LogFactoryUtil.getLog(UserCountCommand.class.getName());
}
