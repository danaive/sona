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
package org.apache.spark.angel.ml.tree.objective.metric;

import javax.inject.Singleton;

@Singleton
public class RMSEMetric extends AverageEvalMetric {
    private static RMSEMetric instance;

    private RMSEMetric() {}

    @Override
    public Kind getKind() {
        return Kind.RMSE;
    }

    @Override
    public double evalOne(float pred, float label) {
        double diff = pred - label;
        return diff * diff;
    }

    @Override
    public double evalOne(float[] pred, float label) {
        double err = 0.0;
        int trueLabel = (int) label;
        for (int i = 0; i < pred.length; i++) {
            double diff = pred[i] - (i == trueLabel ? 1 : 0);
            err += diff * diff;
        }
        return err;
    }

    public static RMSEMetric getInstance() {
        if (instance == null)
            instance = new RMSEMetric();
        return instance;
    }
}
