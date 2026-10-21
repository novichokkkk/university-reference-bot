from __future__ import annotations

import csv
import subprocess
import xml.etree.ElementTree as ET
from datetime import date
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
METRICS_FILE = ROOT / "metrics" / "stage-metrics.csv"


def run_git(*args: str) -> str:
    try:
        result = subprocess.run(
            ["git", *args],
            cwd=ROOT,
            check=True,
            capture_output=True,
            text=True,
        )
        return result.stdout.strip()
    except (OSError, subprocess.CalledProcessError):
        return ""


def checkstyle_errors() -> str:
    report = ROOT / "target" / "checkstyle-result.xml"
    if not report.exists():
        return "NA"
    root = ET.parse(report).getroot()
    return str(len(root.findall(".//error")))


def jacoco_coverage() -> str:
    report = ROOT / "target" / "site" / "jacoco" / "jacoco.csv"
    if not report.exists():
        return "NA"

    covered = missed = 0
    with report.open(encoding="utf-8", newline="") as file:
        for row in csv.DictReader(file):
            covered += int(row["LINE_COVERED"])
            missed += int(row["LINE_MISSED"])

    total = covered + missed
    return f"{covered / total:.1%}" if total else "0.0%"


def test_metrics() -> tuple[str, str, str]:
    report_dir = ROOT / "target" / "surefire-reports"
    total = failed = 0
    duration = 0.0

    for report in report_dir.glob("TEST-*.xml") if report_dir.exists() else []:
        testsuite = ET.parse(report).getroot()
        total += int(testsuite.attrib.get("tests", 0))
        failed += int(testsuite.attrib.get("failures", 0))
        failed += int(testsuite.attrib.get("errors", 0))
        duration += float(testsuite.attrib.get("time", 0))

    return str(total), str(failed), f"{duration:.2f}"


def main() -> None:
    stage = run_git("describe", "--tags", "--always") or "manual"
    commit_count = run_git("rev-list", "--count", "HEAD") or "0"
    tests_total, tests_failed, test_time = test_metrics()

    METRICS_FILE.parent.mkdir(parents=True, exist_ok=True)
    file_exists = METRICS_FILE.exists()
    with METRICS_FILE.open("a", encoding="utf-8", newline="") as file:
        writer = csv.writer(file)
        if not file_exists:
            writer.writerow(
                [
                    "stage",
                    "date",
                    "checkstyle_errors",
                    "jacoco_line_coverage",
                    "tests_total",
                    "tests_failed",
                    "test_time_seconds",
                    "commit_count",
                ]
            )
        writer.writerow(
            [
                stage,
                date.today().isoformat(),
                checkstyle_errors(),
                jacoco_coverage(),
                tests_total,
                tests_failed,
                test_time,
                commit_count,
            ]
        )


if __name__ == "__main__":
    main()
