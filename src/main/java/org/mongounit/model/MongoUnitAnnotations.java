package org.mongounit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.mongounit.AssertMatchesDataset;
import org.mongounit.SeedWithDataset;

/**
 * {@link MongoUnitAnnotations} class is a holder of {@link AssertMatchesDataset} and {@link
 * SeedWithDataset} annotations discovered on a test class or a test method.
 */
public class MongoUnitAnnotations {

  /**
   * List of {@link SeedWithDataset} annotations discovered on a test class or method.
   */
  private List<SeedWithDataset> seedWithDatasetAnnotations;

  /**
   * List of {@link AssertMatchesDataset} annotations discovered on a test class or method.
   */
  private List<AssertMatchesDataset> assertMatchesDatasetAnnotations;

  /**
   * Default constructor.
   */
  public MongoUnitAnnotations() {
    seedWithDatasetAnnotations = new ArrayList<>();
    assertMatchesDatasetAnnotations = new ArrayList<>();
  }

  /**
   * Constructor.
   *
   * Initializes both lists to avoid NPE when adding new items.
   *
   * @param seedWithDatasetAnnotations List of {@link SeedWithDataset} annotations discovered on a
   * test class or method.
   * @param assertMatchesDatasetAnnotations List of {@link AssertMatchesDataset} annotations
   * discovered on a test class or method.
   */
  public MongoUnitAnnotations(
      List<SeedWithDataset> seedWithDatasetAnnotations,
      List<AssertMatchesDataset> assertMatchesDatasetAnnotations) {
    this.seedWithDatasetAnnotations = seedWithDatasetAnnotations;
    this.assertMatchesDatasetAnnotations = assertMatchesDatasetAnnotations;
  }

  /**
   * @return seedWithDatasetAnnotations List of {@link SeedWithDataset} annotations discovered on a
   * test class or method.
   */
  public List<SeedWithDataset> getSeedWithDatasetAnnotations() {
    return seedWithDatasetAnnotations;
  }

  /**
   * @param seedWithDatasetAnnotations seedWithDatasetAnnotations List of {@link SeedWithDataset}
   * annotations discovered on a test class or method.
   */
  public void setSeedWithDatasetAnnotations(
      List<SeedWithDataset> seedWithDatasetAnnotations) {
    this.seedWithDatasetAnnotations = seedWithDatasetAnnotations;
  }

  /**
   * @return List of {@link AssertMatchesDataset} annotations discovered on a test class or method.
   */
  public List<AssertMatchesDataset> getAssertMatchesDatasetAnnotations() {
    return assertMatchesDatasetAnnotations;
  }

  /**
   * @param assertMatchesDatasetAnnotations List of {@link AssertMatchesDataset} annotations
   * discovered on a test class or method.
   */
  public void setAssertMatchesDatasetAnnotations(
      List<AssertMatchesDataset> assertMatchesDatasetAnnotations) {
    this.assertMatchesDatasetAnnotations = assertMatchesDatasetAnnotations;
  }

  /**
   * Adds the provided 'seedWithDatasetAnnotation' to the list of {@link SeedWithDataset}
   * annotations.
   *
   * @param seedWithDatasetAnnotation {@link SeedWithDataset} annotation to add to the list.
   */
  public void addSeedWithDatasetAnnotation(SeedWithDataset seedWithDatasetAnnotation) {

    // Safeguard from NPE
    if (seedWithDatasetAnnotations == null) {
      seedWithDatasetAnnotations = new ArrayList<>();
    }

    seedWithDatasetAnnotations.add(seedWithDatasetAnnotation);
  }

  /**
   * Adds the provided 'seedWithDatasetAnnotations' to the list of {@link SeedWithDataset}
   * annotations.
   *
   * @param seedWithDatasetAnnotations Array of {@link SeedWithDataset} annotations to add to the
   * list.
   */
  public void addSeedWithDatasetAnnotations(SeedWithDataset[] seedWithDatasetAnnotations) {

    // Safeguard from NPE
    if (this.seedWithDatasetAnnotations == null) {
      this.seedWithDatasetAnnotations = new ArrayList<>();
    }

    Collections.addAll(this.seedWithDatasetAnnotations, seedWithDatasetAnnotations);
  }

  /**
   * Adds the provided 'assertMatchesDatasetAnnotation' to the list of {@link AssertMatchesDataset}
   * annotations.
   *
   * @param assertMatchesDatasetAnnotation {@link AssertMatchesDataset} annotation to add to the
   * list.
   */
  public void addAssertMatchesDatasetAnnotation(
      AssertMatchesDataset assertMatchesDatasetAnnotation) {

    // Safeguard from NPE
    if (assertMatchesDatasetAnnotations == null) {
      assertMatchesDatasetAnnotations = new ArrayList<>();
    }

    assertMatchesDatasetAnnotations.add(assertMatchesDatasetAnnotation);
  }

  /**
   * Adds the provided 'assertMatchesDatasetAnnotations' to the list of {@link AssertMatchesDataset}
   * annotations.
   *
   * @param assertMatchesDatasetAnnotations Array of {@link AssertMatchesDataset} annotations to add
   * to the list.
   */
  public void addAssertMatchesDatasetAnnotations(
      AssertMatchesDataset[] assertMatchesDatasetAnnotations) {

    // Safeguard from NPE
    if (this.assertMatchesDatasetAnnotations == null) {
      this.assertMatchesDatasetAnnotations = new ArrayList<>();
    }

    Collections.addAll(this.assertMatchesDatasetAnnotations, assertMatchesDatasetAnnotations);
  }

  @Override
  public String toString() {
    return "MongoUnitAnnotations{" +
        "seedWithDatasetAnnotations=" + seedWithDatasetAnnotations +
        ", assertMatchesDatasetAnnotations=" + assertMatchesDatasetAnnotations +
        '}';
  }
}
