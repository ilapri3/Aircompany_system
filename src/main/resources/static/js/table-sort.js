(() => {
  const parseDateTime = (value) => {
    const trimmed = value.trim();
    const match =
      trimmed.match(/^(\d{2})\.(\d{2})\.(\d{4})(?:\s+(\d{2}):(\d{2}))?/);
    if (!match) {
      return null;
    }

    const [, day, month, year, hours = "00", minutes = "00"] = match;
    return new Date(
      `${year}-${month}-${day}T${hours.padStart(2, "0")}:${minutes.padStart(
        2,
        "0"
      )}:00`
    );
  };

  const normalizeValue = (cell, type) => {
    const text = (cell?.textContent || "").trim();

    if (type === "number") {
      const numeric = parseFloat(text.replace(",", "."));
      return Number.isNaN(numeric) ? text.toLowerCase() : numeric;
    }

    if (type === "date") {
      const date = parseDateTime(text);
      return date ? date.getTime() : text.toLowerCase();
    }

    return text.toLowerCase();
  };

  const detectType = (values) => {
    const sample = values.find((val) => val.trim() !== "");
    if (!sample) {
      return "string";
    }

    if (
      /^-?\d+[.,]?\d*$/.test(sample.trim()) &&
      !Number.isNaN(parseFloat(sample.replace(",", ".")))
    ) {
      return "number";
    }

    if (parseDateTime(sample)) {
      return "date";
    }

    return "string";
  };

  const applySorting = (table) => {
    const tbody = table.querySelector("tbody");
    const headers = Array.from(table.querySelectorAll("th"));

    if (!tbody || !headers.length) {
      return;
    }

    headers.forEach((header, columnIndex) => {
      if (header.dataset.sortable === "false") {
        return;
      }

      header.classList.add("sortable");
      header.addEventListener("click", () => {
        const rows = Array.from(tbody.querySelectorAll("tr"));
        if (!rows.length) {
          return;
        }

        const isAsc = header.dataset.sortDir !== "asc";
        const direction = isAsc ? "asc" : "desc";

        headers.forEach((h) => {
          h.dataset.sortDir = "";
          h.classList.remove("sorted-asc", "sorted-desc");
        });

        header.dataset.sortDir = direction;
        header.classList.add(isAsc ? "sorted-asc" : "sorted-desc");

        const rawValues = rows.map(
          (row) => row.cells[columnIndex]?.textContent || ""
        );
        const type = header.dataset.sortType || detectType(rawValues);

        rows.sort((rowA, rowB) => {
          const a = normalizeValue(rowA.cells[columnIndex], type);
          const b = normalizeValue(rowB.cells[columnIndex], type);

          if (a === b) return 0;
          if (a > b) return isAsc ? 1 : -1;
          return isAsc ? -1 : 1;
        });

        rows.forEach((row) => tbody.appendChild(row));
      });
    });
  };

  document.addEventListener("DOMContentLoaded", () => {
    document
      .querySelectorAll("table.table")
      .forEach((table) => applySorting(table));
  });
})();

