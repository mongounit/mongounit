/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit.model;

import java.util.ArrayList;
import java.util.List;
import org.mongounit.AssertMatchesDataset;
import org.mongounit.SeedWithDataset;

/**
 * {@link MongoUnitDatasets} class contains seed and assert datasets.
 */
public class MongoUnitDatasets {

  /**
   * Datasets to seed the database with.
   */
  private List<MongoUnitCollection> seedWithDatasets;

  /**
   * Datasets to verify matching with.
   */
  private List<MongoUnitCollection> assertMatchesDatasets;

  /**
   * Flag to indicate if {@link AssertMatchesDataset} annotation has been encountered, i.e., the
   * 'assertMatchesDatasets' should actually be used to assert matches, as opposed to having
   * 'assertMatchesDatasets' only there from {@link SeedWithDataset} with 'reuseForAssertion =
   * true'.
   */
  private boolean assertAnnotationPresent;

  /**
   * Default constructor.
   */
  public MongoUnitDatasets() {
    seedWithDatasets = new ArrayList<>();
    assertMatchesDatasets = new ArrayList<>();
    assertAnnotationPresent = false;
  }

  /**
   * Constructor.
   *
   * @param seedWithDatasets Datasets to seed the database with.
   * @param assertMatchesDatasets Datasets to verify matching with.
   * @param assertAnnotationPresent Flag to indicate if {@link AssertMatchesDataset} annotation has
   * been encountered, i.e., the 'assertMatchesDatasets' should actually be used to assert matches,
   * as opposed to having 'assertMatchesDatasets' only there from {@link SeedWithDataset} with
   * 'reuseForAssertion = true'.
   */
  public MongoUnitDatasets(
      List<MongoUnitCollection> seedWithDatasets,
      List<MongoUnitCollection> assertMatchesDatasets, boolean assertAnnotationPresent) {
    this.seedWithDatasets = seedWithDatasets;
    this.assertMatchesDatasets = assertMatchesDatasets;
    this.assertAnnotationPresent = assertAnnotationPresent;
  }

  /**
   * @return Datasets to seed the database with.
   */
  public List<MongoUnitCollection> getSeedWithDatasets() {
    return seedWithDatasets;
  }

  /**
   * @param seedWithDatasets Datasets to seed the database with.
   */
  public void setSeedWithDatasets(
      List<MongoUnitCollection> seedWithDatasets) {
    this.seedWithDatasets = seedWithDatasets;
  }

  /**
   * @return Datasets to verify matching with.
   */
  public List<MongoUnitCollection> getAssertMatchesDatasets() {
    return assertMatchesDatasets;
  }

  /**
   * @param assertMatchesDatasets Datasets to verify matching with.
   */
  public void setAssertMatchesDatasets(
      List<MongoUnitCollection> assertMatchesDatasets) {
    this.assertMatchesDatasets = assertMatchesDatasets;
  }

  /**
   * @return Flag to indicate if {@link AssertMatchesDataset} annotation has been encountered, i.e.,
   * the 'assertMatchesDatasets' should actually be used to assert matches, as opposed to having
   * 'assertMatchesDatasets' only there from {@link SeedWithDataset} with 'reuseForAssertion =
   * true'.
   */
  public boolean isAssertAnnotationPresent() {
    return assertAnnotationPresent;
  }

  /**
   * @param assertAnnotationPresent Flag to indicate if {@link AssertMatchesDataset} annotation has
   * been encountered, i.e., the 'assertMatchesDatasets' should actually be used to assert matches,
   * as opposed to having 'assertMatchesDatasets' only there from {@link SeedWithDataset} with
   * 'reuseForAssertion = true'.
   */
  public void setAssertAnnotationPresent(boolean assertAnnotationPresent) {
    this.assertAnnotationPresent = assertAnnotationPresent;
  }

  @Override
  public String toString() {
    return "MongoUnitDatasets{" +
        "seedWithDatasets=" + seedWithDatasets +
        ", assertMatchesDatasets=" + assertMatchesDatasets +
        ", assertAnnotationPresent=" + assertAnnotationPresent +
        '}';
  }
}
