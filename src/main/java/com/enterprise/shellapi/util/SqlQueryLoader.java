package com.enterprise.shellapi.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Slf4j
@Component
public class SqlQueryLoader {

    private Map<String, Object> queries;

    @PostConstruct
    public void init() {
        try (InputStream is = new ClassPathResource("sql/queries.yml").getInputStream()) {
            Yaml yaml = new Yaml();
            queries = yaml.load(is);
            log.info("Loaded SQL queries from sql/queries.yml");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load SQL queries from sql/queries.yml", e);
        }
    }

    @SuppressWarnings("unchecked")
    public String getQuery(String category, String name) {
        Map<String, String> categoryQueries = (Map<String, String>) queries.get(category);
        if (categoryQueries == null) {
            throw new IllegalArgumentException("SQL query category not found: " + category);
        }
        String query = categoryQueries.get(name);
        if (query == null) {
            throw new IllegalArgumentException("SQL query not found: " + category + "." + name);
        }
        return query.trim();
    }
}
