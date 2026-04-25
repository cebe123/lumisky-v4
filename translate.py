import os
import re

translations = {
    'values-ar': {
        'cat_special': 'خلفيات 4K مميزة',
        'cat_landscapes': 'طبيعة ومناظر 3D',
        'cat_cities': 'سايبربانك ومدن',
        'cat_space': 'فضاء وخيال علمي 4K',
        'cat_abstract': 'خلفيات أنمي حية',
        'cat_games': 'خلفيات ألعاب HD',
        'cat_nature': 'مناظر طبيعية 4K'
    },
    'values-de': {
        'cat_special': 'Premium 4K Hintergrundbilder',
        'cat_landscapes': 'Natur &amp; Landschaften 3D',
        'cat_cities': 'Cyberpunk &amp; Stadtlandschaften',
        'cat_space': 'Weltraum &amp; Sci-Fi 4K',
        'cat_abstract': 'Anime Live-Hintergründe',
        'cat_games': 'Gaming Hintergründe HD',
        'cat_nature': 'Naturlandschaften 4K'
    },
    'values-es': {
        'cat_special': 'Fondos de Pantalla Premium 4K',
        'cat_landscapes': 'Naturaleza y Paisajes 3D',
        'cat_cities': 'Cyberpunk y Ciudades',
        'cat_space': 'Espacio y Ciencia Ficción 4K',
        'cat_abstract': 'Fondos de Pantalla Animados Anime',
        'cat_games': 'Fondos de Pantalla Gaming HD',
        'cat_nature': 'Paisajes Naturales 4K'
    },
    'values-fr': {
        'cat_special': 'Fonds d\'écran Premium 4K',
        'cat_landscapes': 'Nature &amp; Paysages 3D',
        'cat_cities': 'Cyberpunk &amp; Paysages urbains',
        'cat_space': 'Espace &amp; Science-Fiction 4K',
        'cat_abstract': 'Fonds d\'écran Animés Anime',
        'cat_games': 'Fonds d\'écran Gaming HD',
        'cat_nature': 'Paysages Naturels 4K'
    },
    'values-hi': {
        'cat_special': 'प्रीमियम 4K वॉलपेपर',
        'cat_landscapes': 'प्रकृति और परिदृश्य 3D',
        'cat_cities': 'साइबरपंक और शहर',
        'cat_space': 'अंतरिक्ष और विज्ञान-कथा 4K',
        'cat_abstract': 'एनीमे लाइव वॉलपेपर',
        'cat_games': 'गेमिंग बैकग्राउंड HD',
        'cat_nature': 'प्राकृतिक दृश्य 4K'
    },
    'values-it': {
        'cat_special': 'Sfondi Premium 4K',
        'cat_landscapes': 'Natura e Paesaggi 3D',
        'cat_cities': 'Cyberpunk e Città',
        'cat_space': 'Spazio e Fantascienza 4K',
        'cat_abstract': 'Sfondi Animati Anime',
        'cat_games': 'Sfondi Gaming HD',
        'cat_nature': 'Paesaggi Naturali 4K'
    },
    'values-ja': {
        'cat_special': 'プレミアム 4K 壁紙',
        'cat_landscapes': '自然と風景 3D',
        'cat_cities': 'サイバーパンクと都市風景',
        'cat_space': '宇宙とSF 4K',
        'cat_abstract': 'アニメ ライブ壁紙',
        'cat_games': 'ゲーム背景 HD',
        'cat_nature': '自然の風景 4K'
    },
    'values-pt': {
        'cat_special': 'Papéis de Parede Premium 4K',
        'cat_landscapes': 'Natureza e Paisagens 3D',
        'cat_cities': 'Cyberpunk e Cidades',
        'cat_space': 'Espaço e Ficção Científica 4K',
        'cat_abstract': 'Papéis de Parede Animados Anime',
        'cat_games': 'Fundos de Tela Gaming HD',
        'cat_nature': 'Cenários Naturais 4K'
    },
    'values-ru': {
        'cat_special': 'Премиум 4K Обои',
        'cat_landscapes': 'Природа и пейзажи 3D',
        'cat_cities': 'Киберпанк и Города',
        'cat_space': 'Космос и Научная Фантастика 4K',
        'cat_abstract': 'Аниме Живые Обои',
        'cat_games': 'Игровые Фоны HD',
        'cat_nature': 'Природные Пейзажи 4K'
    },
    'values-zh-rCN': {
        'cat_special': '优质 4K 壁纸',
        'cat_landscapes': '自然与风景 3D',
        'cat_cities': '赛博朋克与城市风景',
        'cat_space': '太空与科幻 4K',
        'cat_abstract': '动漫动态壁纸',
        'cat_games': '游戏背景 HD',
        'cat_nature': '自然风光 4K'
    }
}

res_path = r'D:\Lumisky\app\src\main\res'

for folder, trans in translations.items():
    file_path = os.path.join(res_path, folder, 'strings.xml')
    if os.path.exists(file_path):
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        for key, val in trans.items():
            pattern = r'<string name="' + key + r'">.*?</string>'
            replacement = f'<string name="{key}">{val}</string>'
            content = re.sub(pattern, replacement, content, flags=re.DOTALL)
            
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {folder}")
