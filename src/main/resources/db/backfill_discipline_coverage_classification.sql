SELECT dc.id,
       dc.discipline_id,
       dc.domain AS stored_domain,
       dc.tech_family AS stored_family,
       sc.domain AS canonical_domain,
       sc.tech_family AS canonical_family
FROM discipline_coverage dc
JOIN skill_canonical sc ON sc.id = dc.canonical_id
WHERE (dc.domain IS NOT NULL AND dc.domain <> sc.domain)
   OR (dc.tech_family IS NOT NULL AND dc.tech_family IS DISTINCT FROM sc.tech_family)
ORDER BY dc.discipline_id, dc.id;

UPDATE discipline_coverage dc
SET domain = sc.domain
FROM skill_canonical sc
WHERE sc.id = dc.canonical_id
  AND (dc.domain IS NULL OR BTRIM(dc.domain) = '')
  AND sc.domain IS NOT NULL;

UPDATE discipline_coverage dc
SET tech_family = sc.tech_family
FROM skill_canonical sc
WHERE sc.id = dc.canonical_id
  AND (dc.tech_family IS NULL OR BTRIM(dc.tech_family) = '')
  AND sc.tech_family IS NOT NULL;

WITH unique_family_domain AS (
    SELECT tech_family, MIN(domain) AS domain
    FROM skill_canonical
    WHERE tech_family IS NOT NULL AND domain IS NOT NULL
    GROUP BY tech_family
    HAVING COUNT(DISTINCT domain) = 1
)
UPDATE discipline_coverage dc
SET domain = ufd.domain
FROM unique_family_domain ufd
WHERE dc.canonical_id IS NULL
  AND dc.tech_family = ufd.tech_family
  AND (dc.domain IS NULL OR BTRIM(dc.domain) = '');
