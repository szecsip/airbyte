/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.workers.normalization;

import io.airbyte.config.NormalizationInput;
import io.airbyte.config.NormalizationSummary;
import io.airbyte.workers.Worker;

public interface NormalizationWorker extends Worker<NormalizationInput, NormalizationSummary> {}
