package com.ericlam.mc.eld.configurations;

import java.nio.file.Path;
import java.util.Comparator;

public class PageRequest {

    /**
     * 特定數量的指定頁數請求
     * @param page 頁數 (必須從 0 開始)
     * @param size 數量
     * @return 頁數請求
     */
    public static PageRequest of(int page, int size){
        return new PageRequest(page, size);
    }


    /**
     * 特定數量特定排序的指定頁數請求
     * @param page 頁數 (必須從 0 開始)
     * @param size 數量
     * @param comparator 文件路徑排序
     * @return 頁數請求
     */
    public static PageRequest of(int page, int size, Comparator<Path> comparator){
        return new PageRequest(page, size, comparator);
    }

    private final int page;
    private final int size;
    private Comparator<Path> comparator = Path::compareTo;

    /**
     * 特定數量的指定頁數請求
     * @param page 頁數 (必須從 0 開始)
     * @param size 數量
     */
    public PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    /**
     * 特定數量特定排序的指定頁數請求
     * @param page 頁數 (必須從 0 開始)
     * @param size 數量
     * @param comparator 文件名稱排序
     */
    public PageRequest(int page, int size, Comparator<Path> comparator) {
        this.page = page;
        this.size = size;
        this.comparator = comparator;
    }

    /**
     * 獲取頁面請求的頁面數
     * @return 頁面數
     */
    public int getPage() {
        return page;
    }

    /**
     * 獲取每頁數量
     * @return 數量
     */
    public int getSize() {
        return size;
    }

    /**
     * 獲取排序
     * @return 排序
     */
    public Comparator<Path> getComparator() {
        return comparator;
    }

    /**
     * 獲取下一頁的頁面請求
     * @return 頁面請求
     */
    public PageRequest next(){
        return this.withPage(page + 1);
    }

    /**
     * 獲取指定頁數的新頁面請求
     * @param page 新頁數
     * @return 頁面請求
     */
    public PageRequest withPage(int page){
        return PageRequest.of(page, size, comparator);
    }

    /**
     * 獲取上一頁或第一頁的頁面請求
     * @return 頁面請求
     */
    public PageRequest previousOrFirst(){
        return this.withPage(Math.max(0, page - 1));
    }

}
