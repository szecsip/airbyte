/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.db.jdbc.streaming;

import com.google.common.annotations.VisibleForTesting;
import io.airbyte.commons.json.Jsons;

/**
 * Fetch size (number of rows) = target buffer byte size / mean row byte size
 */
public abstract class BaseSizeEstimator implements FetchSizeEstimator {

  // desired buffer size in memory
  private final long targetBufferByteSize;
  private final int minFetchSize;
  private final int defaultFetchSize;
  private final int maxFetchSize;

  // mean byte size per row
  protected double meanByteSize = 0.0;

  protected BaseSizeEstimator(final long targetBufferByteSize,
                              final int minFetchSize,
                              final int defaultFetchSize,
                              final int maxFetchSize) {
    this.targetBufferByteSize = targetBufferByteSize;
    this.minFetchSize = minFetchSize;
    this.defaultFetchSize = defaultFetchSize;
    this.maxFetchSize = maxFetchSize;
  }

  /**
   * What we really want is to know how much memory each {@code rowData} takes. However, there is no
   * easy way to measure that. So we use the byte size of the serialized row to approximate that.
   */
  @VisibleForTesting
  public static long getEstimatedByteSize(final Object rowData) {
    if (rowData == null) {
      return 0L;
    }
    // The string length is multiplied by 4 assuming each character is a
    // full UTF-8 character. In reality, a UTF-8 character is encoded as
    // 1 to 4 bytes. So this is an overestimation. This is alright, because
    // the whole method only provides an estimation. Please never convert
    // the string to byte[] to get the exact length. That conversion is known
    // to introduce a lot of memory overhead.
    return Jsons.serialize(rowData).length() * 4L;
  }

  /**
   * This method ensures that the fetch size is between {@code minFetchSize} and {@code maxFetchSize},
   * inclusively.
   */
  protected int getBoundedFetchSize() {
    if (meanByteSize <= 0.0) {
      return defaultFetchSize;
    }
    final long rawFetchSize = Math.round(targetBufferByteSize / meanByteSize);
    if (rawFetchSize > Integer.MAX_VALUE) {
      return maxFetchSize;
    }
    return Math.max(minFetchSize, Math.min(maxFetchSize, (int) rawFetchSize));
  }

  double getMeanRowByteSize() {
    return meanByteSize;
  }

}
