/*
 * Tencent is pleased to support the open source community by making Angel available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/Apache-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package org.apache.spark.angel.ml.tree.basic.split;

import java.io.Serializable;

/**
 * Different types of tree node splits, enumerated by their complexity.
 *
 */
public enum SplitType implements Serializable {
    SPLIT_POINT("SPLIT_POINT"),
    SPLIT_SET("SPLIT_SET");

    private final String type;

    SplitType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
