package com.test_ebook.interfaces

import android.os.Bundle

/**
 * Created by Ivan Kuzmin on 2019-10-07;
 * 3van@mail.ru;
 * Copyright © 2019 Example. All rights reserved.
 */

interface SaveRestoreInterface {
    fun onSaveInstanceState(outState: Bundle)
    fun onRestoreInstanceState(savedInstanceState: Bundle)
}