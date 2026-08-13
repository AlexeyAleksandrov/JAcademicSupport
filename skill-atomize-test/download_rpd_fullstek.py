"""
Скачивает все PDF-файлы РПД для направления "Фуллстек разработка (2025)"
с сайта https://www.mirea.ru/sveden/education/eduop/

Использование:
    python download_rpd_fullstek.py
    python download_rpd_fullstek.py --out-dir ./rpd_fullstek_2025
    python download_rpd_fullstek.py --dry-run     # только показать ссылки, не качать
"""

import argparse
import os
import re
import sys
import time

import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin, urlparse

BASE_URL = "https://www.mirea.ru/sveden/education/eduop/"
PATTERN = re.compile(r"Fullstek_razrabotka\.pdf$", re.IGNORECASE)
HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/124.0.0.0 Safari/537.36"
    )
}


def fetch_page(url: str) -> BeautifulSoup:
    resp = requests.get(url, headers=HEADERS, timeout=30)
    resp.raise_for_status()
    return BeautifulSoup(resp.text, "html.parser")


def find_rpd_links(soup: BeautifulSoup) -> list[str]:
    """Находит все ссылки на PDF РПД Фуллстек (2025)."""
    links = []
    for tag in soup.find_all("a", href=True):
        href = tag["href"]
        # Берём только .pdf, без .sig
        if PATTERN.search(href):
            full = urljoin(BASE_URL, href)
            if full not in links:
                links.append(full)
    return links


def download_file(url: str, dest_dir: str) -> str:
    filename = os.path.basename(urlparse(url).path)
    dest_path = os.path.join(dest_dir, filename)
    if os.path.exists(dest_path):
        print(f"  [уже есть] {filename}")
        return dest_path
    resp = requests.get(url, headers=HEADERS, timeout=60, stream=True)
    resp.raise_for_status()
    with open(dest_path, "wb") as f:
        for chunk in resp.iter_content(chunk_size=65536):
            f.write(chunk)
    return dest_path


def main():
    parser = argparse.ArgumentParser(description="Скачать РПД Фуллстек 2025 с mirea.ru")
    parser.add_argument("--out-dir", default="rpd_fullstek_2025",
                        help="Папка для сохранения PDF (по умолчанию: rpd_fullstek_2025)")
    parser.add_argument("--dry-run", action="store_true",
                        help="Только показать найденные ссылки, не качать")
    parser.add_argument("--delay", type=float, default=0.5,
                        help="Задержка между запросами в секундах (по умолчанию: 0.5)")
    args = parser.parse_args()

    print(f"Загрузка страницы: {BASE_URL}")
    try:
        soup = fetch_page(BASE_URL)
    except requests.RequestException as e:
        print(f"Ошибка загрузки страницы: {e}", file=sys.stderr)
        sys.exit(1)

    links = find_rpd_links(soup)
    if not links:
        print("PDF-файлы не найдены. Проверь паттерн или структуру страницы.")
        sys.exit(1)

    print(f"\nНайдено PDF-файлов: {len(links)}\n")
    for lnk in links:
        print(f"  {os.path.basename(urlparse(lnk).path)}")

    if args.dry_run:
        print("\n[dry-run] Файлы не скачивались.")
        return

    os.makedirs(args.out_dir, exist_ok=True)
    print(f"\nСохранение в: {os.path.abspath(args.out_dir)}\n")

    ok, fail = 0, 0
    for i, url in enumerate(links, 1):
        fname = os.path.basename(urlparse(url).path)
        print(f"[{i}/{len(links)}] {fname} ... ", end="", flush=True)
        try:
            download_file(url, args.out_dir)
            print("OK")
            ok += 1
        except requests.RequestException as e:
            print(f"ОШИБКА: {e}")
            fail += 1
        if i < len(links):
            time.sleep(args.delay)

    print(f"\nГотово: {ok} скачано, {fail} ошибок.")
    print(f"Файлы в: {os.path.abspath(args.out_dir)}")


if __name__ == "__main__":
    main()
