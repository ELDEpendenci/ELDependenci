package com.ericlam.mc.eld.configurations;

import java.io.File;

/**
 * 獲取根文件的接口，可在以下的類別進行形態轉換使用:
 * <ul>
 *     <li>{@link com.ericlam.mc.eld.controllers.FileController}</li>
 *     <li>{@link GroupConfig}</li>
 *     <li>{@link GroupLang}</li>
 * </ul>
 */
public interface FileLocator {

    /**
     * 獲取文件檔案，如果是文件池則獲取文件池資料夾
     * <p>獲取文件池資料夾: {@link GroupConfig}, {@link GroupLang}</p>
     * <p>獲取文件檔案: {@link com.ericlam.mc.eld.controllers.FileController}</p>
     * @return 文件檔案或文件池資料夾
     */
    File getLocator();

}
