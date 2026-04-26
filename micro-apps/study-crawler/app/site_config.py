from pathlib import Path
from typing import Any

import yaml


class SiteConfigLoader:
    _instance: "SiteConfigLoader | None" = None

    def __new__(cls) -> "SiteConfigLoader":
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._site_config = None
        return cls._instance

    def load(self, config_path: str | Path = "parsing.yaml") -> dict[str, Any]:
        if self._site_config is None:
            path = Path(config_path)
            if not path.is_absolute():
                path = Path(__file__).resolve().parent.parent / path

            with path.open("r", encoding="utf-8") as yaml_file:
                parsed = yaml.safe_load(yaml_file) or {}

            if not isinstance(parsed, dict):
                raise ValueError("parsing.yaml must be a mapping at the root level")

            self._site_config = parsed

        return self._site_config

    def get_site_config(self, config_path: str | Path = "parsing.yaml") -> dict[str, Any]:
        return self.load(config_path)
