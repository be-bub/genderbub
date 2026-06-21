[![README](https://raw.githubusercontent.com/be-bub/genderbub/1.0.0-1.0.3/source/README.png)](https://github.com/be-bub/genderbub/issues)

<p align="center" style="display: flex; gap: 6px; justify-content: center; flex-wrap: wrap;">
  <a href="https://youtu.be/lF4euVOg3-k"><img src="https://raw.githubusercontent.com/be-bub/genderbub/1.0.0-1.0.3/source/Showcase.png" alt="Showcase" width="165" height="auto"></a>
  <a href="https://be-bub.github.io/bub-hub/"><img src="https://raw.githubusercontent.com/be-bub/genderbub/1.0.0-1.0.3/source/Config%20Editor.png" alt="Config Editor" width="165" height="auto"></a>
  <a href="https://github.com/be-bub/genderbub/wiki"><img src="https://raw.githubusercontent.com/be-bub/genderbub/1.0.0-1.0.3/source/Documentation.png" alt="Documentation" width="165" height="auto"></a>
</p>

Each mob gets assigned a random gender upon spawning, with a configurable chance from 0% to 50% for each option. Infertile mobs cannot breed, and the probability is calculated automatically based on your male/female ratio settings. A configurable gender icon appears above mobs and in the HUD when holding the scanner item. You can block same-gender breeding, disable sterile breeding, or allow all without restrictions. Prevents using specific items on certain genders, such as milking a male cow. Villagers receive genders, and this is preserved when curing zombie villagers. Automatically scans for modded animals that extend the Animal class, or you can manually add any animal to the config.

<table style="width: 100%; border: none; background-color: #1A1C20; border-radius: 8px; padding: 10px; margin-bottom: 16px; display: table;">
  <tr>
    <td style="border: none; padding: 12px; text-align: left; font-size: 24px; font-weight: bold; color: #FFFFFF;">
      Can I use it in modpacks?
    </td>
  </tr>
  <tr>
    <td style="border: none; padding: 12px; text-align: left; font-size: 14px; line-height: 1.6; color: #b0b3bc;">
      Yes, you can use this mod in your modpacks without asking for permission or giving credit. The mod automatically scans animals on first launch or with the <code style="background-color: #2a2a30; padding: 2px 6px; border-radius: 4px; color: #b3b3b2;">/bub server scan</code> command, but some mobs may be missed. If a modded animal doesn't get a gender, add its ID manually to the <code style="background-color: #2a2a30; padding: 2px 6px; border-radius: 4px; color: #b3b3b2;">enabledMobs</code> list in the config (inside your Minecraft instance folder). Don't hesitate to report any incompatibility issues with other mods, and always make sure to use the latest version of the mod.
    </td>
  </tr>
</table>

<table style="width: 100%; border: none; background-color: #1A1C20; border-radius: 8px; padding: 10px; margin-bottom: 16px; display: table;">
  <tr>
    <td style="border: none; padding: 12px; text-align: left; font-size: 24px; font-weight: bold; color: #FFFFFF;">
      Can I set up integration myself?
    </td>
  </tr>
  <tr>
    <td style="border: none; padding: 12px; text-align: left; font-size: 14px; line-height: 1.6; color: #b0b3bc;">
      Yes, if you need to add integration with a mod that hasn't been configured yet, you can do it yourself. For example, if you want mob A to always spawn as gender B, go to the <a href="https://be-bub.github.io/genderbub/" style="color: #3b82f6; text-decoration: none; font-weight: 500;">Config Editor</a>, select "Integration Editor", and create a new integration. After that, place the generated files in <code style="background-color: #2a2a30; padding: 2px 6px; border-radius: 4px; color: #b3b3b2;">config/genderbub/integration/compat</code> (inside your Minecraft instance folder) and run the command <code style="background-color: #2a2a30; padding: 2px 6px; border-radius: 4px; color: #b3b3b2;">/bub server integration</code>.
    </td>
  </tr>
</table>

<table style="width: 100%; border: none; background-color: #1A1C20; border-radius: 8px; padding: 10px; margin-bottom: 16px; display: table;">
  <tr>
    <td style="border: none; padding: 12px; text-align: left; font-size: 24px; font-weight: bold; color: #FFFFFF;">
      Compatibility with other mods?
    </td>
  </tr>
  <tr>
    <td style="border: none; padding: 12px; text-align: left; font-size: 14px; line-height: 1.6; color: #b0b3bc;">
      Yes, the mod was designed to expand gameplay and covers mobs from other mods. However, some mods require manual configuration for correct gender distribution. For example, with the <strong style="color: #b3b3b2; font-weight: 500;">Naturalist</strong> mod, lions with manes are male and lions without manes are female. A list of mods that I have already configured can be found here: <a href="https://github.com/be-bub/genderbub/wiki/Compatibility" style="color: #3b82f6; text-decoration: none; font-weight: 500;">Compatibility</a>.
    </td>
  </tr>
</table>

<p align="center">
  English, Русский, Українська, Deutsch, Español, Français, 日本語, 한국어, 简体中文, 繁體中文
  <br>
  <span style="font-size: 12px; color: #7c7f8c;">Commands: English only</span>
</p>

<div align="center">
  
  [![CurseForge](https://img.shields.io/curseforge/dt/1497835?style=for-the-badge&logo=curseforge&logoColor=F16436&label=&labelColor=0c1017&color=0c1017)](https://curseforge.com/minecraft/mc-mods/genderbub)
  [![Modrinth](https://img.shields.io/modrinth/dt/genderbub?style=for-the-badge&logo=modrinth&logoColor=00AF5C&label=&labelColor=0c1017&color=0c1017)](https://modrinth.com/mod/genderbub)
  
</div>
