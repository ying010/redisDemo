package com.wzy.redis.demo.chapter01;

import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ZParams;

import java.util.*;

/**
 * @Package com.wzy.redis.demo.chapter01
 * @ClassName Article
 * @Description TODO
 * @Author W.Z.King
 * @Date 2019/11/5 9:40
 */
public class Article {
    private final static int ONE_WEEK_IN_SECOND = 7 * 24 * 60 * 60;
    private final static int VOTE_SCORE = 432;
    private static final int ARTICLES_PER_PAGE = 25;

    private static final String KEY_ARTICLE = "article:";
    private static final String KEY_USER = "user:";
    private static final String ORDER_BY_TIME = "time:";
    private static final String ORDER_BY_SCORE = "score:";
    private static final String KEY_VOTED = "voted:";
    private static final String KEY_GROUPS = "groups:";
    private static final String KEY_ARTICLE_VOTES = "votes";
    private static final String INCREASE_ARTICLE = "voted:";

    /**
     * 发布文章
     * @param conn
     * @param userId
     * @param title
     * @param link
     * @return
     */
    public String postArticle(Jedis conn, String userId, String title, String link){
        String articleId = String.valueOf(conn.incr(INCREASE_ARTICLE));

        String article = KEY_ARTICLE + articleId;
        Long now = System.currentTimeMillis() / 1000;
        Map<String, String> articleInfo = new HashMap<>(5);
        articleInfo.put("title", title);
        articleInfo.put("poster", KEY_USER + userId);
        articleInfo.put("link", link);
        articleInfo.put("time", String.valueOf(now));
        articleInfo.put("votes", "1");

        conn.hmset(article, articleInfo);
        conn.zadd(ORDER_BY_TIME, now, article);
        conn.zadd(ORDER_BY_SCORE, now + VOTE_SCORE, article);

        conn.sadd(KEY_VOTED + articleId, KEY_USER + userId);
        conn.expire(KEY_VOTED + articleId, ONE_WEEK_IN_SECOND);

        return articleId;
    }

    /**
     * 投票
     * @param conn
     * @param articleId
     * @param userId
     */
    public void articleVote(Jedis conn, String articleId, String userId) {
        Long time = (System.currentTimeMillis() / 1000) - ONE_WEEK_IN_SECOND;
        Double createTime = conn.zscore(ORDER_BY_TIME, KEY_ARTICLE + articleId);
        if (createTime < time) {
            //如果文章发布时间大于7天，不再支持投票
            return;
        }
        Long addFlag = conn.sadd(KEY_VOTED + articleId, KEY_USER + userId);
        if (addFlag < 1) {
            //同一个用户只能对同一篇文章投一次票
            return;
        }
        conn.zincrby(ORDER_BY_SCORE, VOTE_SCORE, KEY_ARTICLE + articleId);
        conn.hincrBy(KEY_ARTICLE + articleId, KEY_ARTICLE_VOTES, 1);
    }

    /**
     * 分页获取文章
     * @param conn
     * @param page
     * @param order 排序方式(排序表的key)
     * @return
     */
    public List<Map<String, String>> getArticles(Jedis conn, int page, String order) {
        int start = (page - 1) * ARTICLES_PER_PAGE;
        int end = page * ARTICLES_PER_PAGE - 1;

        Set<String> articleIds = conn.zrevrange(order, start, end);
        List<Map<String, String>> articles = new ArrayList<>();
        for (String articleId : articleIds) {
            Map<String, String> article = conn.hgetAll(articleId);
            articles.add(article);
        }
        return articles;
    }

    /**
     * 分页获取文章，默认time:排序
     * @param conn
     * @param page
     * @return
     */
    public List<Map<String, String>> getArticles(Jedis conn, int page) {
        return getArticles(conn, page, ORDER_BY_TIME);
    }

    /**
     * 将文章分组
     * @param conn
     * @param articleId 分组的文章
     * @param addGroups 加入的组
     * @param removeGroups 移除的组
     */
    public void addAndRemoveGroups(Jedis conn, String articleId, String[] addGroups, String[] removeGroups){
        for (String group : addGroups) {
            addToGroup(conn, articleId, group);
        }
        for (String group : removeGroups) {
            removeFromGroup(conn, articleId, group);
        }
    }

    /**
     * 将文章加入组
     * @param conn
     * @param articleId
     * @param group
     */
    public void addToGroup(Jedis conn, String articleId, String group) {
        conn.sadd(KEY_GROUPS + group, KEY_ARTICLE + articleId);
    }

    /**
     * 加入多篇文章到同一个组
     * @param conn
     * @param articleIds
     * @param group
     */
    public void addToGroup(Jedis conn, String[] articleIds, String group) {
        List<String> articles = new ArrayList<>();
        for (String articleId : articleIds) {
            articles.add(KEY_ARTICLE + articleId);
        }
        conn.sadd(KEY_GROUPS + group, (String[]) articles.toArray());
    }

    /**
     * 从组中移除文章
     * @param conn
     * @param articleId
     * @param group
     */
    public void removeFromGroup(Jedis conn, String articleId, String group) {
        conn.srem(KEY_GROUPS + group, KEY_ARTICLE + articleId);
    }

    /**
     * 从组中移除多篇文章
     * @param conn
     * @param articleIds
     * @param group
     */
    public void removeFromGroup(Jedis conn, String[] articleIds, String group) {
        List<String> articles = new ArrayList<>();
        for (String articleId : articleIds) {
            articles.add(KEY_ARTICLE + articleId);
        }
        conn.srem(KEY_GROUPS + group, (String[]) articles.toArray());
    }

    /**
     * 获取组中的文章
     * @param conn
     * @param group
     * @param page
     * @param order
     * @return
     */
    public List<Map<String, String>> getArticlesFromGroup(Jedis conn, String group, int page, String order) {
        String key = order + group;
        if (!conn.exists(key)) {
            ZParams zParams = new ZParams();
            zParams.aggregate(ZParams.Aggregate.MAX);
            conn.zinterstore(key, zParams, KEY_GROUPS + group, order);
            conn.expire(key, 60);
        }

        return getArticles(conn, page, key);
    }

    public List<Map<String, String>> getArticlesFromGroup(Jedis conn, String group, int page) {
        return getArticlesFromGroup(conn, group, page, ORDER_BY_SCORE);
    }

    public void printArticle(List<Map<String, String>> articles) {
        System.out.println("》》》》》》》》》》》》》》》》》》》");
        for (Map<String, String> article : articles) {
            for (Map.Entry<String, String> entry : article.entrySet()) {
                System.out.println("--->" + entry.getKey() + ": " + entry.getValue());
            }
        }
    }

    @Test
    public void run() {
        Jedis jedis = new Jedis("148.70.65.167");
        jedis.select(1);

        String articleId = postArticle(jedis, "user1", "a title", "www.baidu.com");
        Map<String, String> article = jedis.hgetAll(KEY_ARTICLE + articleId);
        for (Map.Entry<String, String> entry : article.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }

        articleVote(jedis, articleId, "user2");
        String votes = jedis.hget(KEY_ARTICLE + articleId, KEY_ARTICLE_VOTES);
        System.out.println("投票成功，文章总票数：" + votes);
        assert Integer.parseInt(votes) > 1;

        List<Map<String, String>> articles = getArticles(jedis, 1);
        printArticle(articles);
        assert articles.size() > 0;

        addToGroup(jedis, articleId, "programming");
        List<Map<String, String>> groupArticles = getArticlesFromGroup(jedis, "programming", 1);
        printArticle(groupArticles);
        assert groupArticles.size() > 0;

    }

}
