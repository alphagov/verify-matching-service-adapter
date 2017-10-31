# 2. Hash pids uniquely for each LoA

Date: 15/05/2017

## Status

Accepted

## Context

The pid that the user is issued by their identity provider will not necessarily change when the level of assurance they achieve changes. For example a user may uplift from LoA1 to LoA2 in their IdP, but this will present to the relying party with the same identifier. This is an issue because a Cycle 0 match at LoA1 is not valid for use at LoA2, and presents a specific security risk whereby a user may obtain LoA1 as someone else, person A, and then uplift to LoA2 using their own identity (person B), but continue to assume the identity of person A at the higher LoA. So a change of LoA requires matching to be done afresh.

## Decision

We will modify the pid for non-LoA2 assertions to be unique to that LoA so that it is not possible for relying parties to make an insecure match at Cycle 0 when the user moves between levels of assurance. This will be done by introducing the LoA into the pid hash.

## Consequences

The consequences are that relying parties working with multiple LoAs will be forced to completely rematch the user when they present with a new LoA. This is consistent with what we already require and instruct relying parties to do. There are no relying parties currently accepting multiple LoAs, so there is no historic expectation that users can be matched by pid between LoAs.
