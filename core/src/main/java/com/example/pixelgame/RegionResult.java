package com.example.pixelgame;

/** Immutable region totals include all failed attempts and exclude modal time. */
record RegionResult(int stage, float seconds, int damage, int potions, int retries,
                    int attacks, int skills, int dashes, boolean cleared) {}
