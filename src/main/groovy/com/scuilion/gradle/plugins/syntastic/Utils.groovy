package com.scuilion.gradle.plugins.syntastic

import com.google.common.base.Strings

class Utils {
    private static final String OS = System.getProperty('os.name').toLowerCase()
    private static final boolean isWindows = (OS.indexOf('win') >= 0)
    public static final boolean isCygwin = (isWindows && !Strings.isNullOrEmpty(System.getenv('TERM')))
}
