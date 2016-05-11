package com.adelcastillo.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import org.osgi.service.component.annotations.Component;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.comment.CommentManagerUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.IdentityServiceContextFunction;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.ratings.kernel.service.RatingsEntryLocalServiceUtil;

@Component(immediate = true, property = { "osgi.command.function=addComents",
		"osgi.command.scope=blade" }, service = Object.class)
public class AddComentsRandom {

	public void addComents(String articleId, long groupId, boolean addRatings, int numComments) {
		try {
			JournalArticle article = JournalArticleLocalServiceUtil.getLatestArticle(groupId, articleId);
			String className = JournalArticle.class.getName();

			IdentityServiceContextFunction testServiceContext = new IdentityServiceContextFunction(_serviceContext);

			List<User> listUsers = UserLocalServiceUtil.getCompanyUsers(article.getCompanyId(), -1, -1);
			for (int i = 0; i <= numComments; i++) {
				User user = listUsers.get(random.nextInt(numUsers));
				String body = generateBody(random.nextInt(10));
				long classPK = article.getResourcePrimKey();

				CommentManagerUtil.addComment(user.getUserId(), article.getGroupId(), className, classPK, body,
						testServiceContext);

				if (addRatings) {
					double score = 0.8;
					RatingsEntryLocalServiceUtil.updateEntry(user.getUserId(), className, classPK, score,
							_serviceContext);
				}

			}

		} catch (PortalException e) {
			_log.error("Error while accesing the content> " + e);
		}
	}

	public void addComents(String articleId, long groupId, boolean addRatings) {
		int ranNumber = random.nextInt(numUsers);
		int numComments = checkNumUsers(ranNumber);
		addComents(articleId, groupId, addRatings, numComments);
	}

	private int checkNumUsers(int number) {
		if (number > numUsers)
			return numUsers;
		else
			return number;
	}

	public void addComents(String articleId, long groupId) {
		addComents(articleId, groupId, true, 10);
	}

	public String generateBody(int numPar) {
		String body = "";
		String sURL = "http://loripsum.net/api/" + numPar + "/short/headers";
		URL url;
		HttpURLConnection conn; // The actual connection to the web page
		BufferedReader rd; // Used to read results from the web page
		String line; // An individual line of the web page HTML

		try {
			url = new URL(sURL);

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				body += line;
			}
			rd.close();

		} catch (MalformedURLException e) {
			_log.error("Error> " + e);
		} catch (IOException e) {
			_log.error("Error> " + e);
		}
		return body;
	}

	private int numUsers = UserLocalServiceUtil.getUsersCount();

	private Random random = new Random();

	private final ServiceContext _serviceContext = new ServiceContext();

	private Log _log = LogFactoryUtil.getLog(UserCountCommand.class.getName());
}
