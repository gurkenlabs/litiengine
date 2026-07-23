package de.gurkenlabs.utiliti.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

final class InspectorNavigationHistory<T> {
  private final List<T> entries = new ArrayList<>();
  private int index;

  InspectorNavigationHistory(T initialTarget) {
    this.entries.add(initialTarget);
  }

  void record(T target) {
    if (Objects.equals(this.entries.get(this.index), target)) {
      return;
    }
    if (this.index < this.entries.size() - 1) {
      this.entries.subList(this.index + 1, this.entries.size()).clear();
    }
    this.entries.add(target);
    this.index = this.entries.size() - 1;
  }

  boolean canGoBack(Predicate<T> isValid) {
    return this.findPrevious(isValid) >= 0;
  }

  boolean canGoForward(Predicate<T> isValid) {
    return this.findNext(isValid) >= 0;
  }

  T goBack(Predicate<T> isValid) {
    this.index = this.findPrevious(isValid);
    return this.entries.get(this.index);
  }

  T goForward(Predicate<T> isValid) {
    this.index = this.findNext(isValid);
    return this.entries.get(this.index);
  }

  private int findPrevious(Predicate<T> isValid) {
    for (int i = this.index - 1; i >= 0; i--) {
      if (isValid.test(this.entries.get(i))) {
        return i;
      }
    }
    return -1;
  }

  private int findNext(Predicate<T> isValid) {
    for (int i = this.index + 1; i < this.entries.size(); i++) {
      if (isValid.test(this.entries.get(i))) {
        return i;
      }
    }
    return -1;
  }
}
