package edu.java.clients;

import edu.java.Utils;
import edu.java.dao.LinksToUpdateDao;
import edu.java.dto.stackoverflow.StackOverflowUpdatesDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowClient extends AbstractClient<StackOverflowUpdatesDto> {

    private final LinksToUpdateDao linksToUpdateDao;
    private final Logger logger = LoggerFactory.getLogger(GitHubClient.class);

    private static final String BASE_URL = "https://api.stackexchange.com/2.3/questions/";

    @Override
    protected void onReceipt(String s, StackOverflowUpdatesDto dto) {
        var lastModified = linksToUpdateDao.get(s);
        if (lastModified == null || !lastModified.equals(dto.getItems().getFirst().toString())) {
            linksToUpdateDao.save(s, dto.getItems().getFirst().toString());
            logger.info("Updates by link: " + s + ": " + dto.getItems());
        } else {
            logger.info("No updates by link: " + s + ": " + dto.getItems());
        }
    }

    public StackOverflowClient(String baseUrl, LinksToUpdateDao dao) {
        super(baseUrl == null ? BASE_URL : baseUrl);
        this.linksToUpdateDao = dao;
        this.classMono = StackOverflowUpdatesDto.class;
    }

    @Override
    protected String transform(String link) {
        return Utils.stackOverflowLinkToUri(link);
    }

}
