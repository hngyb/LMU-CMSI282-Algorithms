package csp;

import java.time.LocalDate;
import java.util.*;

/**
 * CSP: Calendar Satisfaction Problem Solver Provides a solution for scheduling
 * some n meetings in a given period of time and according to some set of unary
 * and binary constraints on the dates of each meeting.
 */
public class CSP {

	/**
	 * Public interface for the CSP solver in which the number of meetings, range of
	 * allowable dates for each meeting, and constraints on meeting times are
	 * specified.
	 * 
	 * @param nMeetings   The number of meetings that must be scheduled, indexed
	 *                    from 0 to n-1
	 * @param rangeStart  The start date (inclusive) of the domains of each of the n
	 *                    meeting-variables
	 * @param rangeEnd    The end date (inclusive) of the domains of each of the n
	 *                    meeting-variables
	 * @param constraints Date constraints on the meeting times (unary and binary
	 *                    for this assignment)
	 * @return A list of dates that satisfies each of the constraints for each of
	 *         the n meetings, indexed by the variable they satisfy, or null if no
	 *         solution exists.
	 */
	public static List<LocalDate> solve(int nMeetings, LocalDate rangeStart, LocalDate rangeEnd,
			Set<DateConstraint> constraints) {
		List<DateVar> domains = new ArrayList<>();
		List<LocalDate> result = new ArrayList<>();

		// Model domains
		domains = modelDomains(nMeetings, rangeStart, rangeEnd);

		// Implement Node Consistency Preprocessing
		nodeConsistency(domains, constraints);

		// Implement AC-3 Preprocessing
		arcConsistency(domains, constraints);

		// Perform backtracking and return a solution
		return backtracking(nMeetings, result, domains, constraints);
	}

	private static void nodeConsistency(List<DateVar> domains, Set<DateConstraint> constraints) {
		for (DateConstraint d : constraints) {
			Set<LocalDate> left = domains.get(d.L_VAL).domain;
			Set<LocalDate> toRemove = new HashSet<>();
			if (d.arity() == 1) {
				LocalDate right = ((UnaryDateConstraint) d).R_VAL;
				for (LocalDate l : left) {
					switch (d.OP) {
					case "==":
						if (!l.isEqual(right))
							toRemove.add(l);
						break;
					case "!=":
						if (!l.isEqual(right))
							toRemove.add(right);
						break;
					case ">":
						if (!l.isAfter(right))
							toRemove.add(l);
						break;
					case "<":
						if (!l.isBefore(right))
							toRemove.add(l);
						break;
					case ">=":
						if (l.isBefore(right))
							toRemove.add(l);
						break;
					case "<=":
						if (l.isAfter(right))
							toRemove.add(l);
						break;
					}
				}
				left.removeAll(toRemove);
			}
		}
	}

	private static void arcConsistency(List<DateVar> domains, Set<DateConstraint> constraints) {
		for (DateConstraint d : constraints) {
			Set<LocalDate> left = domains.get(d.L_VAL).domain;
			Set<LocalDate> toRemove = new HashSet<>();
			if (d.arity() == 2) {
				Set<LocalDate> right = domains.get(((BinaryDateConstraint) d).R_VAL).domain;
				for (LocalDate l : left) {
					boolean sat = false;
					for (LocalDate r : right) {
						switch (d.OP) {
						case "==":
							if (l.isEqual(r))
								sat = true;
							break;
						case "!=":
							if (!l.isEqual(r))
								sat = true;
							break;
						case ">":
							if (l.isAfter(r))
								sat = true;
							break;
						case "<":
							if (l.isBefore(r))
								sat = true;
							break;
						case ">=":
							if (l.isAfter(r) || l.isEqual(r))
								sat = true;
							break;
						case "<=":
							if (l.isBefore(r) || l.isEqual(r))
								sat = true;
							break;
						}
					}
					if (!sat) {
						toRemove.add(l);
					}
				}
				left.removeAll(toRemove);
				toRemove.clear();

				for (LocalDate r : right) {
					boolean sat = false;
					for (LocalDate l : left) {
						switch (d.OP) {
						case "==":
							if (r.isEqual(l))
								sat = true;
							break;
						case "!=":
							if (!r.isEqual(l))
								sat = true;
							break;
						case ">":
							if (r.isBefore(l))
								sat = true;
							break;
						case "<":
							if (r.isAfter(l))
								sat = true;
							break;
						case ">=":
							if (r.isBefore(l) || r.isEqual(l))
								sat = true;
							break;
						case "<=":
							if (r.isAfter(l) || r.isEqual(l))
								sat = true;
							break;
						}
					}
					if (!sat) {
						toRemove.add(r);
					}
				}
				right.removeAll(toRemove);
			}
		}
	}

	private static List<LocalDate> backtracking(int nMeetings, List<LocalDate> result, List<DateVar> domains,
			Set<DateConstraint> constraints) {
		int index = result.size();
		if (index == nMeetings) {
			if (testSolution(result, constraints)) {
				return result;
			} else {
				return null;
			}
		}
		for (LocalDate d : domains.get(index).domain) {
			result.add(d);
			List<LocalDate> solution = backtracking(nMeetings, result, domains, constraints);
			if (solution != null) {
				return solution;
			}
			result.remove(index);
		}
		return null;
	}

	public static boolean testSolution(List<LocalDate> soln, Set<DateConstraint> constraints) {
		for (DateConstraint d : constraints) {
			LocalDate leftDate = soln.get(d.L_VAL), rightDate = (d.arity() == 1) ? ((UnaryDateConstraint) d).R_VAL
					: soln.get(((BinaryDateConstraint) d).R_VAL);

			boolean sat = false;
			switch (d.OP) {
			case "==":
				if (leftDate.isEqual(rightDate))
					sat = true;
				break;
			case "!=":
				if (!leftDate.isEqual(rightDate))
					sat = true;
				break;
			case ">":
				if (leftDate.isAfter(rightDate))
					sat = true;
				break;
			case "<":
				if (leftDate.isBefore(rightDate))
					sat = true;
				break;
			case ">=":
				if (leftDate.isAfter(rightDate) || leftDate.isEqual(rightDate))
					sat = true;
				break;
			case "<=":
				if (leftDate.isBefore(rightDate) || leftDate.isEqual(rightDate))
					sat = true;
				break;
			}
			if (!sat) {
				return false;
			}
		}
		return true;
	}

	private static List<DateVar> modelDomains(int nMeetings, LocalDate rangeStart, LocalDate rangeEnd) {
		List<DateVar> domains = new ArrayList<>();
		Set<LocalDate> tempDomain = new HashSet<LocalDate>();

		for (LocalDate date = rangeStart; !date.isAfter(rangeEnd); date = date.plusDays(1)) {
			tempDomain.add(date);
		}
		for (int i = 0; i < nMeetings; i++) {
			domains.add(new DateVar(new HashSet<LocalDate>(tempDomain)));
		}
		return domains;
	}

	private static class DateVar {
		Set<LocalDate> domain;

		DateVar(Set<LocalDate> domain) {
			this.domain = domain;
		}
	}
}
