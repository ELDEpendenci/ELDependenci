package com.ericlam.mc.eld.configurations;

import java.util.List;
import java.util.function.Function;

/**
 * 頁面 (參考了 Spring Data Page)
 * @param <T> 元素類型
 */
public interface Page<T> {

    /**
     * 獲取本頁的所有內容
     * @return 所有內容
     */
    List<T> getContent();

    /**
     *
     * @return 是否有內容
     */
    boolean hasContent();

    /**
     * 獲取目前頁面數
     * @return 頁面數
     */
    int getCurrentPage();

    /**
     * 獲取總共頁面數
     * @return 總共頁面數
     */
    int getTotalPages();

    /**
     * 是否有下一頁
     * @return this
     */
    boolean hasNext();

    /**
     * 是否有上一頁
     * @return this
     */
    boolean hasPrevious();

    /**
     * 獲取所有頁面加起來的總共數量
     * @return 總共數量
     */
    long getTotalElements();

    /**
     * 獲取頁面請求
     * @return 頁面請求
     */
    PageRequest getPageRequest();

    /**
     * 轉換形態用
     * @param converter 轉換
     * @param <U> 新形態
     * @return this
     */
    <U> Page<U> map(Function<T, U> converter);

}
