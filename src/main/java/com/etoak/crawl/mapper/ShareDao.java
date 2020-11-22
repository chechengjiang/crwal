package com.etoak.crawl.mapper;

import com.etoak.crawl.page.Share;

import java.util.List;
import java.util.Map;

public interface ShareDao {

    public List<Map<String, String>> testList(Map<String, String> param);

    public void insertSharesDaily(List<Share> shareList);
}
