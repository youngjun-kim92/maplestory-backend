-- 보스 마스터 데이터를 최신 패치 기준으로 초기화합니다.
-- reset_type: daily(매일 초기화), weekly(매주 목요일), monthly(매월 1일)
-- max_attempts_per_week: daily=7, weekly=1, monthly=1
DELETE FROM boss_master;

INSERT INTO boss_master (boss_name, difficulty, crystal_price, max_attempts_per_week, reset_type, max_party_size) VALUES

-- ────────────────────────────────────────────────────────────
-- 일일 보스 (최대 6명)
-- ────────────────────────────────────────────────────────────
('자쿰',     'easy',   200000,   7, 'daily', 6),
('자쿰',     'normal', 612500,   7, 'daily', 6),
('파풀라투스', 'easy',   684500,   7, 'daily', 6),
('파풀라투스', 'normal', 2664500,  7, 'daily', 6),
('매그너스',  'easy',   722000,   7, 'daily', 6),
('매그너스',  'normal', 2592000,  7, 'daily', 6),
('혼테일',   'easy',   882000,   7, 'daily', 6),
('혼테일',   'normal', 1012500,  7, 'daily', 6),
('혼테일',   'chaos',  1352000,  7, 'daily', 6),
('아카이럼',  'easy',   1152000,  7, 'daily', 6),
('아카이럼',  'normal', 2520500,  7, 'daily', 6),
('반레온',   'easy',   1058000,  7, 'daily', 6),
('반레온',   'normal', 1458000,  7, 'daily', 6),
('반레온',   'hard',   2450000,  7, 'daily', 6),
('블러디퀸',  'normal', 968000,   7, 'daily', 6),
('피에르',   'normal', 968000,   7, 'daily', 6),
('반반',     'normal', 968000,   7, 'daily', 6),
('벨룸',     'normal', 968000,   7, 'daily', 6),
('핑크빈',   'normal', 1404500,  7, 'daily', 6),
('힐라',     'normal', 800000,   7, 'daily', 6),
('카웅',     'normal', 1250000,  7, 'daily', 6),

-- ────────────────────────────────────────────────────────────
-- 주간 보스 (매주 목요일 초기화)
-- ────────────────────────────────────────────────────────────
('시그너스',      'easy',    4550000,    1, 'weekly', 6),
('시그너스',      'normal',  7500000,    1, 'weekly', 6),
('힐라',          'hard',    5750000,    1, 'weekly', 6),
('핑크빈',        'chaos',   6580000,    1, 'weekly', 6),
('자쿰',          'chaos',   8080000,    1, 'weekly', 6),
('블러디퀸',      'chaos',   8140000,    1, 'weekly', 6),
('피에르',        'chaos',   8140000,    1, 'weekly', 6),
('반반',          'chaos',   8170000,    1, 'weekly', 6),
('매그너스',      'hard',    8560000,    1, 'weekly', 6),
('벨룸',          'chaos',   9280000,    1, 'weekly', 6),
('파풀라투스',    'chaos',   13800000,   1, 'weekly', 6),
('스우',          'normal',  17600000,   1, 'weekly', 6),
('스우',          'hard',    54200000,   1, 'weekly', 6),
('스우',          'extreme', 604000000,  1, 'weekly', 2),
('데미안',        'normal',  18400000,   1, 'weekly', 6),
('데미안',        'hard',    51500000,   1, 'weekly', 6),
('가디언 엔젤 슬라임', 'normal', 26800000, 1, 'weekly', 6),
('가디언 엔젤 슬라임', 'chaos',  79100000, 1, 'weekly', 6),
('루시드',        'easy',    31400000,   1, 'weekly', 6),
('루시드',        'normal',  37500000,   1, 'weekly', 6),
('루시드',        'hard',    66200000,   1, 'weekly', 6),
('윌',            'easy',    34000000,   1, 'weekly', 6),
('윌',            'normal',  43300000,   1, 'weekly', 6),
('윌',            'hard',    81200000,   1, 'weekly', 6),
('더스크',        'normal',  46300000,   1, 'weekly', 6),
('더스크',        'chaos',   73500000,   1, 'weekly', 6),
('듄켈',          'normal',  50000000,   1, 'weekly', 6),
('듄켈',          'hard',    99400000,   1, 'weekly', 6),
('진 힐라',       'normal',  74900000,   1, 'weekly', 6),
('진 힐라',       'hard',    112000000,  1, 'weekly', 6),
('선택받은 세렌', 'normal',  266000000,  1, 'weekly', 6),
('선택받은 세렌', 'hard',    396000000,  1, 'weekly', 6),
('선택받은 세렌', 'extreme', 3150000000, 1, 'weekly', 6),
('감시자 칼로스', 'easy',    311000000,  1, 'weekly', 6),
('감시자 칼로스', 'normal',  561000000,  1, 'weekly', 6),
('감시자 칼로스', 'chaos',   1340000000, 1, 'weekly', 6),
('감시자 칼로스', 'extreme', 4320000000, 1, 'weekly', 6),
('최초의 대적자', 'easy',    324000000,  1, 'weekly', 6),
('최초의 대적자', 'normal',  589000000,  1, 'weekly', 6),
('최초의 대적자', 'hard',    1510000000, 1, 'weekly', 6),
('최초의 대적자', 'extreme', 4960000000, 1, 'weekly', 6),
('카링',          'easy',    419000000,  1, 'weekly', 6),
('카링',          'normal',  714000000,  1, 'weekly', 6),
('카링',          'hard',    1830000000, 1, 'weekly', 6),
('찬란한 흉성',   'normal',  658000000,  1, 'weekly', 3),
('찬란한 흉성',   'hard',    2819000000, 1, 'weekly', 3),
('림보',          'normal',  1080000000, 1, 'weekly', 3),
('림보',          'hard',    2510000000, 1, 'weekly', 3),
('발드릭스',      'normal',  1440000000, 1, 'weekly', 3),
('발드릭스',      'hard',    3240000000, 1, 'weekly', 3),
('유피테르',      'normal',  1700000000, 1, 'weekly', 3),
('유피테르',      'hard',    5100000000, 1, 'weekly', 3),

-- ────────────────────────────────────────────────────────────
-- 월간 보스 (매월 1일 초기화)
-- ────────────────────────────────────────────────────────────
('검은 마법사',   'hard',    700000000,  1, 'monthly', 6),
('검은 마법사',   'extreme', 9200000000, 1, 'monthly', 6);

-- ────────────────────────────────────────────────────────────
-- 보스 드랍 물욕템 마스터 데이터
-- item_category: dark_accessory(칠흑), radiant_accessory(광휘), dawn_accessory(여명), other(기타)
-- ────────────────────────────────────────────────────────────
DELETE FROM boss_drop_master;

INSERT INTO boss_drop_master (boss_name, difficulty, item_name, item_category) VALUES
-- 스우
('스우', 'hard',    '루즈 컨트롤 머신 마크',   'dark_accessory'),
('스우', 'extreme', '루즈 컨트롤 머신 마크',   'dark_accessory'),
('스우', 'extreme', '컴플리트 언더컨트롤',     'dark_accessory'),

-- 데미안
('데미안', 'hard',  '마력이 깃든 안대',        'dark_accessory'),

-- 가디언 엔젤 슬라임
('가디언 엔젤 슬라임', 'normal', '가디언 엔젤 링', 'dawn_accessory'),
('가디언 엔젤 슬라임', 'chaos',  '가디언 엔젤 링', 'dawn_accessory'),

-- 루시드
('루시드', 'hard', '몽환의 벨트',              'dark_accessory'),
('루시드', 'hard', '트와일라이트 마크',         'dawn_accessory'),

-- 윌
('윌', 'hard', '저주받은 마도서',              'dark_accessory'),
('윌', 'hard', '트와일라이트 마크',            'dawn_accessory'),

-- 더스크
('더스크', 'chaos', '거대한 공포',             'dark_accessory'),
('더스크', 'chaos', '에스텔라 이어링',         'dawn_accessory'),

-- 듄켈
('듄켈', 'hard', '커맨더 포스 이어링',         'dark_accessory'),
('듄켈', 'hard', '에스텔라 이어링',            'dawn_accessory'),

-- 진 힐라
('진 힐라', 'hard', '고통의 근원',             'dark_accessory'),
('진 힐라', 'hard', '데이브레이크 펜던트',      'dawn_accessory'),

-- 검은 마법사
('검은 마법사', 'hard',    '창세의 뱃지',             'dark_accessory'),
('검은 마법사', 'extreme', '창세의 뱃지',             'dark_accessory'),
('검은 마법사', 'extreme', '익셉셔널 해머 (벨트)',     'other'),

-- 선택받은 세렌
('선택받은 세렌', 'hard',    '미트라의 분노',            'dark_accessory'),
('선택받은 세렌', 'hard',    '데이브레이크 펜던트',       'dawn_accessory'),
('선택받은 세렌', 'extreme', '미트라의 분노',            'dark_accessory'),
('선택받은 세렌', 'extreme', '데이브레이크 펜던트',       'dawn_accessory'),
('선택받은 세렌', 'extreme', '익셉셔널 해머 (얼굴장식)', 'other'),

-- 감시자 칼로스
('감시자 칼로스', 'chaos',   '생명의 연마석',            'other'),
('감시자 칼로스', 'chaos',   '생명의 보스 반지 상자',     'other'),
('감시자 칼로스', 'extreme', '생명의 연마석',            'other'),
('감시자 칼로스', 'extreme', '생명의 보스 반지 상자',     'other'),
('감시자 칼로스', 'extreme', '익셉셔널 해머 (눈장식)',    'other'),

-- 카링
('카링', 'hard',    '신념의 연마석',            'other'),
('카링', 'hard',    '신념의 보스 반지 상자',     'other'),
('카링', 'extreme', '신념의 연마석',            'other'),
('카링', 'extreme', '신념의 보스 반지 상자',     'other'),
('카링', 'extreme', '익셉셔널 해머 (귀고리)',    'other'),

-- 찬란한 흉성
('찬란한 흉성', 'hard', '황홀한 악몽',          'radiant_accessory'),
('찬란한 흉성', 'hard', '생명의 연마석',         'other'),
('찬란한 흉성', 'hard', '백옥의 보스 반지 상자', 'other'),

-- 최초의 대적자
('최초의 대적자', 'hard',    '불멸의 유산',              'radiant_accessory'),
('최초의 대적자', 'extreme', '불멸의 유산',              'radiant_accessory'),
('최초의 대적자', 'extreme', '익셉셔널 해머 (훈장)',      'other'),

-- 림보
('림보', 'hard', '근원의 속삭임',              'radiant_accessory'),

-- 발드릭스
('발드릭스', 'hard', '죽음의 맹세',             'radiant_accessory'),

-- 유피테르
('유피테르', 'hard', '오만의 원죄',             'radiant_accessory');

-- ────────────────────────────────────────────────────────────
-- 도핑 영약 마스터 데이터
-- ────────────────────────────────────────────────────────────
DELETE FROM doping_master;

INSERT INTO doping_master (name, effect, price, sort_order) VALUES
('세이람의 영약',   '하울링 + 어드밴스드 블레스(하이퍼 포함) + 샤프 아이즈 효과 (15분, 동일 파티 버프와 중첩 불가)',   2000000,  1),
('알레리아의 영약', '치명적인 타격 시 1회 부활 (시간 제한 없음, 화중군자 등 부활류 스킬과 중첩 불가)',                2000000,  2),
('명예의 영약',     '공격력 및 마력 +60 (30분)',                                                                       5000000,  3),
('콜렉터의 영약',   '최대 HP/MP +400, 공격력/마력 +100, 올스탯 +30, 스킬 레벨 +2 (8분)',                             20000000, 4);
