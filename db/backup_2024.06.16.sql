PGDMP      %                |            AcademicSupport    16.3    16.3 l    L           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                      false            M           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                      false            N           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                      false            O           1262    16394    AcademicSupport    DATABASE     �   CREATE DATABASE "AcademicSupport" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Russian_Russia.1251';
 !   DROP DATABASE "AcademicSupport";
                postgres    false            �            1259    16399 
   competency    TABLE     �   CREATE TABLE public.competency (
    id bigint NOT NULL,
    number character varying(5) NOT NULL,
    description text NOT NULL
);
    DROP TABLE public.competency;
       public         heap    postgres    false            �            1259    16404     competency_achievement_indicator    TABLE     o  CREATE TABLE public.competency_achievement_indicator (
    id bigint NOT NULL,
    description character varying(255) NOT NULL,
    indicator_know character varying(255) NOT NULL,
    indicator_able character varying(255) NOT NULL,
    indicator_possess character varying(255) NOT NULL,
    number character varying(10) NOT NULL,
    competency_id bigint NOT NULL
);
 4   DROP TABLE public.competency_achievement_indicator;
       public         heap    postgres    false            �            1259    16409 )   competency_achievement_indicator_keywords    TABLE     �   CREATE TABLE public.competency_achievement_indicator_keywords (
    competency_achievement_indicator_id bigint NOT NULL,
    keywords_id bigint NOT NULL
);
 =   DROP TABLE public.competency_achievement_indicator_keywords;
       public         heap    postgres    false            �            1259    16412    competency_id_seq    SEQUENCE     z   CREATE SEQUENCE public.competency_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 (   DROP SEQUENCE public.competency_id_seq;
       public          postgres    false    215            P           0    0    competency_id_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.competency_id_seq OWNED BY public.competency.id;
          public          postgres    false    218            �            1259    16413    competency_keywords    TABLE     p   CREATE TABLE public.competency_keywords (
    competency_id bigint NOT NULL,
    keywords_id bigint NOT NULL
);
 '   DROP TABLE public.competency_keywords;
       public         heap    postgres    false            �            1259    16416    keyword    TABLE     S   CREATE TABLE public.keyword (
    id bigint NOT NULL,
    keyword text NOT NULL
);
    DROP TABLE public.keyword;
       public         heap    postgres    false            �            1259    16421    keyword_id_seq    SEQUENCE     w   CREATE SEQUENCE public.keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 %   DROP SEQUENCE public.keyword_id_seq;
       public          postgres    false    220            Q           0    0    keyword_id_seq    SEQUENCE OWNED BY     A   ALTER SEQUENCE public.keyword_id_seq OWNED BY public.keyword.id;
          public          postgres    false    221            �            1259    16422    keyword_work_skills    TABLE     p   CREATE TABLE public.keyword_work_skills (
    keyword_id bigint NOT NULL,
    work_skills_id bigint NOT NULL
);
 '   DROP TABLE public.keyword_work_skills;
       public         heap    postgres    false            �            1259    16425    recommended_skills    TABLE     �   CREATE TABLE public.recommended_skills (
    id bigint NOT NULL,
    work_skill_id bigint NOT NULL,
    coefficient double precision DEFAULT 0.0 NOT NULL,
    rpd_id bigint
);
 &   DROP TABLE public.recommended_skills;
       public         heap    postgres    false            �            1259    16429    recommended_skills_id_seq    SEQUENCE     �   CREATE SEQUENCE public.recommended_skills_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 0   DROP SEQUENCE public.recommended_skills_id_seq;
       public          postgres    false    223            R           0    0    recommended_skills_id_seq    SEQUENCE OWNED BY     W   ALTER SEQUENCE public.recommended_skills_id_seq OWNED BY public.recommended_skills.id;
          public          postgres    false    224            �            1259    16430    rpd    TABLE     r   CREATE TABLE public.rpd (
    id bigint NOT NULL,
    discipline_name text NOT NULL,
    year integer NOT NULL
);
    DROP TABLE public.rpd;
       public         heap    postgres    false            �            1259    16435 %   rpd_competency_achievement_indicators    TABLE     �   CREATE TABLE public.rpd_competency_achievement_indicators (
    rpd_id bigint NOT NULL,
    competency_achievement_indicators_id bigint NOT NULL
);
 9   DROP TABLE public.rpd_competency_achievement_indicators;
       public         heap    postgres    false            �            1259    16438 
   rpd_id_seq    SEQUENCE     s   CREATE SEQUENCE public.rpd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 !   DROP SEQUENCE public.rpd_id_seq;
       public          postgres    false    225            S           0    0 
   rpd_id_seq    SEQUENCE OWNED BY     9   ALTER SEQUENCE public.rpd_id_seq OWNED BY public.rpd.id;
          public          postgres    false    227            �            1259    16439 -   rpd_keywords_for_indicator_in_context_rpd_map    TABLE     �   CREATE TABLE public.rpd_keywords_for_indicator_in_context_rpd_map (
    rpd_id bigint NOT NULL,
    keywords_for_indicator_in_context_rpd_map_id bigint NOT NULL,
    keywords_for_indicator_in_context_rpd_map_key bigint NOT NULL
);
 A   DROP TABLE public.rpd_keywords_for_indicator_in_context_rpd_map;
       public         heap    postgres    false            �            1259    16442    rpd_recommended_skills    TABLE     v   CREATE TABLE public.rpd_recommended_skills (
    rpd_id bigint NOT NULL,
    recommended_skills_id bigint NOT NULL
);
 *   DROP TABLE public.rpd_recommended_skills;
       public         heap    postgres    false            �            1259    16445    rpd_recommended_work_skills    TABLE     �   CREATE TABLE public.rpd_recommended_work_skills (
    rpd_id bigint NOT NULL,
    recommended_work_skills_id bigint NOT NULL
);
 /   DROP TABLE public.rpd_recommended_work_skills;
       public         heap    postgres    false            �            1259    16448    skills_group    TABLE     �   CREATE TABLE public.skills_group (
    id bigint NOT NULL,
    description character varying(255),
    market_demand double precision DEFAULT 0
);
     DROP TABLE public.skills_group;
       public         heap    postgres    false            �            1259    16451    skills_group_id_seq    SEQUENCE     |   CREATE SEQUENCE public.skills_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 *   DROP SEQUENCE public.skills_group_id_seq;
       public          postgres    false    231            T           0    0    skills_group_id_seq    SEQUENCE OWNED BY     K   ALTER SEQUENCE public.skills_group_id_seq OWNED BY public.skills_group.id;
          public          postgres    false    232            �            1259    16452 *   subcompetency_achievement_indicator_id_seq    SEQUENCE     �   CREATE SEQUENCE public.subcompetency_achievement_indicator_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 A   DROP SEQUENCE public.subcompetency_achievement_indicator_id_seq;
       public          postgres    false    216            U           0    0 *   subcompetency_achievement_indicator_id_seq    SEQUENCE OWNED BY     v   ALTER SEQUENCE public.subcompetency_achievement_indicator_id_seq OWNED BY public.competency_achievement_indicator.id;
          public          postgres    false    233            �            1259    16453    vacancy    TABLE     �   CREATE TABLE public.vacancy (
    id bigint NOT NULL,
    name text NOT NULL,
    published_at text,
    hh_id bigint NOT NULL,
    description text
);
    DROP TABLE public.vacancy;
       public         heap    postgres    false            �            1259    16458    vacancy_id_seq    SEQUENCE     w   CREATE SEQUENCE public.vacancy_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 %   DROP SEQUENCE public.vacancy_id_seq;
       public          postgres    false    234            V           0    0    vacancy_id_seq    SEQUENCE OWNED BY     A   ALTER SEQUENCE public.vacancy_id_seq OWNED BY public.vacancy.id;
          public          postgres    false    235            �            1259    16459    vacancy_skills    TABLE     m   CREATE TABLE public.vacancy_skills (
    skills_id bigint NOT NULL,
    vacancy_entity_id bigint NOT NULL
);
 "   DROP TABLE public.vacancy_skills;
       public         heap    postgres    false            �            1259    16462 
   work_skill    TABLE     �   CREATE TABLE public.work_skill (
    id bigint NOT NULL,
    description character varying(255),
    market_demand double precision,
    skills_group_id bigint DEFAULT '-1'::integer NOT NULL
);
    DROP TABLE public.work_skill;
       public         heap    postgres    false            �            1259    16466    work_skill_id_seq    SEQUENCE     z   CREATE SEQUENCE public.work_skill_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 (   DROP SEQUENCE public.work_skill_id_seq;
       public          postgres    false    237            W           0    0    work_skill_id_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.work_skill_id_seq OWNED BY public.work_skill.id;
          public          postgres    false    238            ]           2604    16467    competency id    DEFAULT     n   ALTER TABLE ONLY public.competency ALTER COLUMN id SET DEFAULT nextval('public.competency_id_seq'::regclass);
 <   ALTER TABLE public.competency ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    218    215            ^           2604    16468 #   competency_achievement_indicator id    DEFAULT     �   ALTER TABLE ONLY public.competency_achievement_indicator ALTER COLUMN id SET DEFAULT nextval('public.subcompetency_achievement_indicator_id_seq'::regclass);
 R   ALTER TABLE public.competency_achievement_indicator ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    233    216            _           2604    16469 
   keyword id    DEFAULT     h   ALTER TABLE ONLY public.keyword ALTER COLUMN id SET DEFAULT nextval('public.keyword_id_seq'::regclass);
 9   ALTER TABLE public.keyword ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    221    220            `           2604    16470    recommended_skills id    DEFAULT     ~   ALTER TABLE ONLY public.recommended_skills ALTER COLUMN id SET DEFAULT nextval('public.recommended_skills_id_seq'::regclass);
 D   ALTER TABLE public.recommended_skills ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    224    223            b           2604    16471    rpd id    DEFAULT     `   ALTER TABLE ONLY public.rpd ALTER COLUMN id SET DEFAULT nextval('public.rpd_id_seq'::regclass);
 5   ALTER TABLE public.rpd ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    227    225            c           2604    16472    skills_group id    DEFAULT     r   ALTER TABLE ONLY public.skills_group ALTER COLUMN id SET DEFAULT nextval('public.skills_group_id_seq'::regclass);
 >   ALTER TABLE public.skills_group ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    232    231            e           2604    16473 
   vacancy id    DEFAULT     h   ALTER TABLE ONLY public.vacancy ALTER COLUMN id SET DEFAULT nextval('public.vacancy_id_seq'::regclass);
 9   ALTER TABLE public.vacancy ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    235    234            f           2604    16474    work_skill id    DEFAULT     n   ALTER TABLE ONLY public.work_skill ALTER COLUMN id SET DEFAULT nextval('public.work_skill_id_seq'::regclass);
 <   ALTER TABLE public.work_skill ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    238    237            2          0    16399 
   competency 
   TABLE DATA           =   COPY public.competency (id, number, description) FROM stdin;
    public          postgres    false    215   .�       3          0    16404     competency_achievement_indicator 
   TABLE DATA           �   COPY public.competency_achievement_indicator (id, description, indicator_know, indicator_able, indicator_possess, number, competency_id) FROM stdin;
    public          postgres    false    216   ȏ       4          0    16409 )   competency_achievement_indicator_keywords 
   TABLE DATA           u   COPY public.competency_achievement_indicator_keywords (competency_achievement_indicator_id, keywords_id) FROM stdin;
    public          postgres    false    217   a�       6          0    16413    competency_keywords 
   TABLE DATA           I   COPY public.competency_keywords (competency_id, keywords_id) FROM stdin;
    public          postgres    false    219   ��       7          0    16416    keyword 
   TABLE DATA           .   COPY public.keyword (id, keyword) FROM stdin;
    public          postgres    false    220    �       9          0    16422    keyword_work_skills 
   TABLE DATA           I   COPY public.keyword_work_skills (keyword_id, work_skills_id) FROM stdin;
    public          postgres    false    222   ��       :          0    16425    recommended_skills 
   TABLE DATA           T   COPY public.recommended_skills (id, work_skill_id, coefficient, rpd_id) FROM stdin;
    public          postgres    false    223   )�       <          0    16430    rpd 
   TABLE DATA           8   COPY public.rpd (id, discipline_name, year) FROM stdin;
    public          postgres    false    225   �       =          0    16435 %   rpd_competency_achievement_indicators 
   TABLE DATA           m   COPY public.rpd_competency_achievement_indicators (rpd_id, competency_achievement_indicators_id) FROM stdin;
    public          postgres    false    226   |�       ?          0    16439 -   rpd_keywords_for_indicator_in_context_rpd_map 
   TABLE DATA           �   COPY public.rpd_keywords_for_indicator_in_context_rpd_map (rpd_id, keywords_for_indicator_in_context_rpd_map_id, keywords_for_indicator_in_context_rpd_map_key) FROM stdin;
    public          postgres    false    228   ��       @          0    16442    rpd_recommended_skills 
   TABLE DATA           O   COPY public.rpd_recommended_skills (rpd_id, recommended_skills_id) FROM stdin;
    public          postgres    false    229   љ       A          0    16445    rpd_recommended_work_skills 
   TABLE DATA           Y   COPY public.rpd_recommended_work_skills (rpd_id, recommended_work_skills_id) FROM stdin;
    public          postgres    false    230   �       B          0    16448    skills_group 
   TABLE DATA           F   COPY public.skills_group (id, description, market_demand) FROM stdin;
    public          postgres    false    231   �       E          0    16453    vacancy 
   TABLE DATA           M   COPY public.vacancy (id, name, published_at, hh_id, description) FROM stdin;
    public          postgres    false    234   m�       G          0    16459    vacancy_skills 
   TABLE DATA           F   COPY public.vacancy_skills (skills_id, vacancy_entity_id) FROM stdin;
    public          postgres    false    236   /      H          0    16462 
   work_skill 
   TABLE DATA           U   COPY public.work_skill (id, description, market_demand, skills_group_id) FROM stdin;
    public          postgres    false    237   *0      X           0    0    competency_id_seq    SEQUENCE SET     @   SELECT pg_catalog.setval('public.competency_id_seq', 15, true);
          public          postgres    false    218            Y           0    0    keyword_id_seq    SEQUENCE SET     =   SELECT pg_catalog.setval('public.keyword_id_seq', 79, true);
          public          postgres    false    221            Z           0    0    recommended_skills_id_seq    SEQUENCE SET     J   SELECT pg_catalog.setval('public.recommended_skills_id_seq', 2597, true);
          public          postgres    false    224            [           0    0 
   rpd_id_seq    SEQUENCE SET     8   SELECT pg_catalog.setval('public.rpd_id_seq', 9, true);
          public          postgres    false    227            \           0    0    skills_group_id_seq    SEQUENCE SET     B   SELECT pg_catalog.setval('public.skills_group_id_seq', 17, true);
          public          postgres    false    232            ]           0    0 *   subcompetency_achievement_indicator_id_seq    SEQUENCE SET     X   SELECT pg_catalog.setval('public.subcompetency_achievement_indicator_id_seq', 7, true);
          public          postgres    false    233            ^           0    0    vacancy_id_seq    SEQUENCE SET     =   SELECT pg_catalog.setval('public.vacancy_id_seq', 35, true);
          public          postgres    false    235            _           0    0    work_skill_id_seq    SEQUENCE SET     A   SELECT pg_catalog.setval('public.work_skill_id_seq', 141, true);
          public          postgres    false    238            o           2606    16485 L   competency_achievement_indicator competency_achievement_indicator_number_key 
   CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT competency_achievement_indicator_number_key UNIQUE (number);
 v   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT competency_achievement_indicator_number_key;
       public            postgres    false    216            q           2606    16487 F   competency_achievement_indicator competency_achievement_indicator_pkey 
   CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT competency_achievement_indicator_pkey PRIMARY KEY (id);
 p   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT competency_achievement_indicator_pkey;
       public            postgres    false    216            i           2606    16489    competency competency_id_key 
   CONSTRAINT     U   ALTER TABLE ONLY public.competency
    ADD CONSTRAINT competency_id_key UNIQUE (id);
 F   ALTER TABLE ONLY public.competency DROP CONSTRAINT competency_id_key;
       public            postgres    false    215            k           2606    16491     competency competency_number_key 
   CONSTRAINT     ]   ALTER TABLE ONLY public.competency
    ADD CONSTRAINT competency_number_key UNIQUE (number);
 J   ALTER TABLE ONLY public.competency DROP CONSTRAINT competency_number_key;
       public            postgres    false    215            m           2606    16493    competency competency_pkey 
   CONSTRAINT     X   ALTER TABLE ONLY public.competency
    ADD CONSTRAINT competency_pkey PRIMARY KEY (id);
 D   ALTER TABLE ONLY public.competency DROP CONSTRAINT competency_pkey;
       public            postgres    false    215            u           2606    24978    keyword keyword_keyword_key 
   CONSTRAINT     Y   ALTER TABLE ONLY public.keyword
    ADD CONSTRAINT keyword_keyword_key UNIQUE (keyword);
 E   ALTER TABLE ONLY public.keyword DROP CONSTRAINT keyword_keyword_key;
       public            postgres    false    220            w           2606    16497    keyword keyword_pkey 
   CONSTRAINT     R   ALTER TABLE ONLY public.keyword
    ADD CONSTRAINT keyword_pkey PRIMARY KEY (id);
 >   ALTER TABLE ONLY public.keyword DROP CONSTRAINT keyword_pkey;
       public            postgres    false    220            y           2606    16499 ,   recommended_skills recommended_skills_id_key 
   CONSTRAINT     e   ALTER TABLE ONLY public.recommended_skills
    ADD CONSTRAINT recommended_skills_id_key UNIQUE (id);
 V   ALTER TABLE ONLY public.recommended_skills DROP CONSTRAINT recommended_skills_id_key;
       public            postgres    false    223            {           2606    16501 (   recommended_skills recommended_skills_pk 
   CONSTRAINT     f   ALTER TABLE ONLY public.recommended_skills
    ADD CONSTRAINT recommended_skills_pk PRIMARY KEY (id);
 R   ALTER TABLE ONLY public.recommended_skills DROP CONSTRAINT recommended_skills_pk;
       public            postgres    false    223                       2606    16503 `   rpd_keywords_for_indicator_in_context_rpd_map rpd_keywords_for_indicator_in_context_rpd_map_pkey 
   CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT rpd_keywords_for_indicator_in_context_rpd_map_pkey PRIMARY KEY (rpd_id, keywords_for_indicator_in_context_rpd_map_key);
 �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT rpd_keywords_for_indicator_in_context_rpd_map_pkey;
       public            postgres    false    228    228            }           2606    16505    rpd rpd_pkey 
   CONSTRAINT     J   ALTER TABLE ONLY public.rpd
    ADD CONSTRAINT rpd_pkey PRIMARY KEY (id);
 6   ALTER TABLE ONLY public.rpd DROP CONSTRAINT rpd_pkey;
       public            postgres    false    225            �           2606    16507     skills_group skills_group_id_key 
   CONSTRAINT     Y   ALTER TABLE ONLY public.skills_group
    ADD CONSTRAINT skills_group_id_key UNIQUE (id);
 J   ALTER TABLE ONLY public.skills_group DROP CONSTRAINT skills_group_id_key;
       public            postgres    false    231            �           2606    16509    skills_group skills_group_pk 
   CONSTRAINT     Z   ALTER TABLE ONLY public.skills_group
    ADD CONSTRAINT skills_group_pk PRIMARY KEY (id);
 F   ALTER TABLE ONLY public.skills_group DROP CONSTRAINT skills_group_pk;
       public            postgres    false    231            s           2606    16511 K   competency_achievement_indicator subcompetency_achievement_indicator_id_key 
   CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT subcompetency_achievement_indicator_id_key UNIQUE (id);
 u   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT subcompetency_achievement_indicator_id_key;
       public            postgres    false    216            �           2606    16513 7   rpd_recommended_work_skills uk425s9mbq3ruhfaib3ln6s19cr 
   CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_work_skills
    ADD CONSTRAINT uk425s9mbq3ruhfaib3ln6s19cr UNIQUE (recommended_work_skills_id);
 a   ALTER TABLE ONLY public.rpd_recommended_work_skills DROP CONSTRAINT uk425s9mbq3ruhfaib3ln6s19cr;
       public            postgres    false    230            �           2606    16515    vacancy vacancy_hh_id_key 
   CONSTRAINT     U   ALTER TABLE ONLY public.vacancy
    ADD CONSTRAINT vacancy_hh_id_key UNIQUE (hh_id);
 C   ALTER TABLE ONLY public.vacancy DROP CONSTRAINT vacancy_hh_id_key;
       public            postgres    false    234            �           2606    16517    vacancy vacancy_pkey 
   CONSTRAINT     R   ALTER TABLE ONLY public.vacancy
    ADD CONSTRAINT vacancy_pkey PRIMARY KEY (id);
 >   ALTER TABLE ONLY public.vacancy DROP CONSTRAINT vacancy_pkey;
       public            postgres    false    234            �           2606    16519    work_skill work_skill_id_key 
   CONSTRAINT     U   ALTER TABLE ONLY public.work_skill
    ADD CONSTRAINT work_skill_id_key UNIQUE (id);
 F   ALTER TABLE ONLY public.work_skill DROP CONSTRAINT work_skill_id_key;
       public            postgres    false    237            �           2606    16521    work_skill work_skill_pkey 
   CONSTRAINT     X   ALTER TABLE ONLY public.work_skill
    ADD CONSTRAINT work_skill_pkey PRIMARY KEY (id);
 D   ALTER TABLE ONLY public.work_skill DROP CONSTRAINT work_skill_pkey;
       public            postgres    false    237            �           2606    16522 R   competency_achievement_indicator competency_achievement_indicator_competency_id_fk    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT competency_achievement_indicator_competency_id_fk FOREIGN KEY (competency_id) REFERENCES public.competency(id);
 |   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT competency_achievement_indicator_competency_id_fk;
       public          postgres    false    4713    215    216            �           2606    16527 I   rpd_keywords_for_indicator_in_context_rpd_map fk1sb6pdj6rjp9elxtc5ef0tige    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT fk1sb6pdj6rjp9elxtc5ef0tige FOREIGN KEY (keywords_for_indicator_in_context_rpd_map_id) REFERENCES public.keyword(id);
 s   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT fk1sb6pdj6rjp9elxtc5ef0tige;
       public          postgres    false    4727    220    228            �           2606    16532 H   rpd_keywords_for_indicator_in_context_rpd_map fk2quehu5yoisgnidf2kikdnlp    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT fk2quehu5yoisgnidf2kikdnlp FOREIGN KEY (rpd_id) REFERENCES public.rpd(id);
 r   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT fk2quehu5yoisgnidf2kikdnlp;
       public          postgres    false    4733    228    225            �           2606    16537 *   vacancy_skills fk3lxhcg6nbdf1gq6gau2qwrgfp    FK CONSTRAINT     �   ALTER TABLE ONLY public.vacancy_skills
    ADD CONSTRAINT fk3lxhcg6nbdf1gq6gau2qwrgfp FOREIGN KEY (skills_id) REFERENCES public.work_skill(id) ON UPDATE CASCADE ON DELETE CASCADE;
 T   ALTER TABLE ONLY public.vacancy_skills DROP CONSTRAINT fk3lxhcg6nbdf1gq6gau2qwrgfp;
       public          postgres    false    237    236    4747            �           2606    16542 /   keyword_work_skills fk4k5q1ts47obcutvixud6r5jol    FK CONSTRAINT     �   ALTER TABLE ONLY public.keyword_work_skills
    ADD CONSTRAINT fk4k5q1ts47obcutvixud6r5jol FOREIGN KEY (work_skills_id) REFERENCES public.work_skill(id) ON UPDATE CASCADE ON DELETE CASCADE;
 Y   ALTER TABLE ONLY public.keyword_work_skills DROP CONSTRAINT fk4k5q1ts47obcutvixud6r5jol;
       public          postgres    false    4747    237    222            �           2606    16547 E   competency_achievement_indicator_keywords fk6ihuse5jevucy5liuoh2w6ovs    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator_keywords
    ADD CONSTRAINT fk6ihuse5jevucy5liuoh2w6ovs FOREIGN KEY (competency_achievement_indicator_id) REFERENCES public.competency_achievement_indicator(id);
 o   ALTER TABLE ONLY public.competency_achievement_indicator_keywords DROP CONSTRAINT fk6ihuse5jevucy5liuoh2w6ovs;
       public          postgres    false    216    217    4721            �           2606    24846 7   rpd_recommended_work_skills fk9pygp1m1u9yxesq9a8wtl86nh    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_work_skills
    ADD CONSTRAINT fk9pygp1m1u9yxesq9a8wtl86nh FOREIGN KEY (rpd_id) REFERENCES public.rpd(id) ON UPDATE CASCADE ON DELETE CASCADE;
 a   ALTER TABLE ONLY public.rpd_recommended_work_skills DROP CONSTRAINT fk9pygp1m1u9yxesq9a8wtl86nh;
       public          postgres    false    225    230    4733            �           2606    16557 .   recommended_skills fk9wms8xsw6d3nenoa5xe2ma16w    FK CONSTRAINT     �   ALTER TABLE ONLY public.recommended_skills
    ADD CONSTRAINT fk9wms8xsw6d3nenoa5xe2ma16w FOREIGN KEY (rpd_id) REFERENCES public.rpd(id);
 X   ALTER TABLE ONLY public.recommended_skills DROP CONSTRAINT fk9wms8xsw6d3nenoa5xe2ma16w;
       public          postgres    false    225    4733    223            �           2606    16562 A   rpd_competency_achievement_indicators fk9yincl0yk3dec9dqfr76c9w9s    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_competency_achievement_indicators
    ADD CONSTRAINT fk9yincl0yk3dec9dqfr76c9w9s FOREIGN KEY (rpd_id) REFERENCES public.rpd(id);
 k   ALTER TABLE ONLY public.rpd_competency_achievement_indicators DROP CONSTRAINT fk9yincl0yk3dec9dqfr76c9w9s;
       public          postgres    false    4733    225    226            �           2606    16567 7   rpd_recommended_work_skills fkdl0wgopbuj6j7l387wyiw0pe3    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_work_skills
    ADD CONSTRAINT fkdl0wgopbuj6j7l387wyiw0pe3 FOREIGN KEY (recommended_work_skills_id) REFERENCES public.work_skill(id) ON UPDATE CASCADE ON DELETE CASCADE;
 a   ALTER TABLE ONLY public.rpd_recommended_work_skills DROP CONSTRAINT fkdl0wgopbuj6j7l387wyiw0pe3;
       public          postgres    false    4747    230    237            �           2606    16572 A   rpd_competency_achievement_indicators fkdoed4n0dilyh3bb0dp2hjk20f    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_competency_achievement_indicators
    ADD CONSTRAINT fkdoed4n0dilyh3bb0dp2hjk20f FOREIGN KEY (competency_achievement_indicators_id) REFERENCES public.competency_achievement_indicator(id);
 k   ALTER TABLE ONLY public.rpd_competency_achievement_indicators DROP CONSTRAINT fkdoed4n0dilyh3bb0dp2hjk20f;
       public          postgres    false    216    4721    226            �           2606    16577 /   keyword_work_skills fkfc8xqj21xypdk76tgtrkfexmr    FK CONSTRAINT     �   ALTER TABLE ONLY public.keyword_work_skills
    ADD CONSTRAINT fkfc8xqj21xypdk76tgtrkfexmr FOREIGN KEY (keyword_id) REFERENCES public.keyword(id);
 Y   ALTER TABLE ONLY public.keyword_work_skills DROP CONSTRAINT fkfc8xqj21xypdk76tgtrkfexmr;
       public          postgres    false    222    4727    220            �           2606    16582 I   rpd_keywords_for_indicator_in_context_rpd_map fkgfpvx2jsey6jlhoqoogsjg25l    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT fkgfpvx2jsey6jlhoqoogsjg25l FOREIGN KEY (keywords_for_indicator_in_context_rpd_map_key) REFERENCES public.competency_achievement_indicator(id);
 s   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT fkgfpvx2jsey6jlhoqoogsjg25l;
       public          postgres    false    216    228    4721            �           2606    16587 E   competency_achievement_indicator_keywords fkhux4o70yjb812s53ka1bcwwav    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator_keywords
    ADD CONSTRAINT fkhux4o70yjb812s53ka1bcwwav FOREIGN KEY (keywords_id) REFERENCES public.keyword(id) ON UPDATE CASCADE ON DELETE CASCADE;
 o   ALTER TABLE ONLY public.competency_achievement_indicator_keywords DROP CONSTRAINT fkhux4o70yjb812s53ka1bcwwav;
       public          postgres    false    220    4727    217            �           2606    16592 2   rpd_recommended_skills fkj7wqecaxj4bxe1kbftr48q2ig    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_skills
    ADD CONSTRAINT fkj7wqecaxj4bxe1kbftr48q2ig FOREIGN KEY (rpd_id) REFERENCES public.rpd(id);
 \   ALTER TABLE ONLY public.rpd_recommended_skills DROP CONSTRAINT fkj7wqecaxj4bxe1kbftr48q2ig;
       public          postgres    false    229    4733    225            �           2606    16597 *   vacancy_skills fkja9glj7ao8pyy3k59p56iuyt3    FK CONSTRAINT     �   ALTER TABLE ONLY public.vacancy_skills
    ADD CONSTRAINT fkja9glj7ao8pyy3k59p56iuyt3 FOREIGN KEY (vacancy_entity_id) REFERENCES public.vacancy(id);
 T   ALTER TABLE ONLY public.vacancy_skills DROP CONSTRAINT fkja9glj7ao8pyy3k59p56iuyt3;
       public          postgres    false    236    234    4745            �           2606    16602 /   competency_keywords fkopdasmcmo9iw4gges2ignt0tx    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_keywords
    ADD CONSTRAINT fkopdasmcmo9iw4gges2ignt0tx FOREIGN KEY (keywords_id) REFERENCES public.keyword(id) ON UPDATE CASCADE ON DELETE CASCADE;
 Y   ALTER TABLE ONLY public.competency_keywords DROP CONSTRAINT fkopdasmcmo9iw4gges2ignt0tx;
       public          postgres    false    4727    219    220            �           2606    16607 /   competency_keywords fkptcv7s0w2swcpk6han7ta3q9s    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_keywords
    ADD CONSTRAINT fkptcv7s0w2swcpk6han7ta3q9s FOREIGN KEY (competency_id) REFERENCES public.competency(id);
 Y   ALTER TABLE ONLY public.competency_keywords DROP CONSTRAINT fkptcv7s0w2swcpk6han7ta3q9s;
       public          postgres    false    219    4713    215            �           2606    16612 2   rpd_recommended_skills fkquv63vg9xvwng9i5w6rrai9mw    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_skills
    ADD CONSTRAINT fkquv63vg9xvwng9i5w6rrai9mw FOREIGN KEY (recommended_skills_id) REFERENCES public.recommended_skills(id);
 \   ALTER TABLE ONLY public.rpd_recommended_skills DROP CONSTRAINT fkquv63vg9xvwng9i5w6rrai9mw;
       public          postgres    false    229    4729    223            �           2606    16617 6   recommended_skills recommended_skills_work_skill_id_fk    FK CONSTRAINT     �   ALTER TABLE ONLY public.recommended_skills
    ADD CONSTRAINT recommended_skills_work_skill_id_fk FOREIGN KEY (work_skill_id) REFERENCES public.work_skill(id) ON UPDATE CASCADE ON DELETE CASCADE;
 `   ALTER TABLE ONLY public.recommended_skills DROP CONSTRAINT recommended_skills_work_skill_id_fk;
       public          postgres    false    237    223    4747            �           2606    16622 (   work_skill work_skill_skills_group_id_fk    FK CONSTRAINT     �   ALTER TABLE ONLY public.work_skill
    ADD CONSTRAINT work_skill_skills_group_id_fk FOREIGN KEY (skills_group_id) REFERENCES public.skills_group(id);
 R   ALTER TABLE ONLY public.work_skill DROP CONSTRAINT work_skill_skills_group_id_fk;
       public          postgres    false    4739    237    231            2   �  x��UQn�@��O��D�JN���W�a0��ʑ��G��0���
�7�{����i���ݝy3o�ͤ����C���o�źR����CΣ�H/���'\�	������q�al�=\�p�?�*7���l&�CW;<���n����5w��p� =~�y��|O����J���$�I�3�.�Q3����ld���iTB�t�``���Q��ܺ/
j�E��-6�2px�
�� ��Ӓ��Ko>dE��pهѧ�5|s��Q�������5�A�/����c��
ob�`m|�e��n�8�����(%/_K��J���A
�������h�˄�T��EGU��7\�w��fc(�#��S���>��mb���ױ`���|��)�V+\��X�i�WP��J�W�������y��WC!9C6�z��I��ػ�@kنe@:Չ��5��O���FQkNeu`sT�=3=�0��dD�Ɨ�cs����3�������1��h,�?s_���q�Ⱦ��	�i�5	i�1��򃨕�Gҷ*!��zHl�X9��u8���?#+ �_�����p*| ���l���RqMxNTۼ��u9�8nZ?��-����2���E���h�P��\{H�BD�nL�jtǒ�U�]h6$�}|�e�o�eB      3   �  x��W[N�@��O�,H	�\f�PX��k�ˮ����q��Bύ��f�P2Q("`�LOMuu�0ȓK*�$R�GI�44)%��ľF`!I�Q����\"s��	�)�^㱔5~�LCy��y5��e���*"���R!V"ib&X�&�0鞙V�!1���~��SI��o��$C S5�R��@n�}�W��[jD�^iD�ٱff�Akp��x�V�pؿ����q�Tb]�Ү�x已=����:���9>Q��٥��
��$P�� �*-V�8�p�|�&y�_'���`��w��˶�v�������ȩ�lR�@���afL��'E  �sݔN� ����I�ڻ��8�����-~�{l�w?51 ���
ٌA0���J�f��Q��]y��i���k���t4�3%�`��i�҅�L�"d�kA��8����N;����'@;ۋR���+�ML^5�9.wb�Hw��7<�1��=�`Q���X���ZcT�s��E���\�2�y�<k �6UKZ�� ��u��;
��y�A�g�*Ț^e�����������e�⛘�6w�6�����f��qh�Yn+�-��
r�fb�+bY�^�~{�h�Di�h1/l�U���J=�wk�<ŵ�U��,C���G�k�݋;ߣ��^s��j�ؐQ-��j8g�E�����n�!�w��rGBS��N�cO<�Kn�vSz���mm�#����4����d�������,L���])�;�kI>��\ŷ�����c=�R=�<��_�RԽk�W�(��=���c���W7ݛ����|Z�#5o�?G�%y`��kEҨD�٣�v�TY�i�Պ�vw�\n�l�[��ͽ㮶q����7�-�4ߚT�4I�;�Ru�DZ�R�?��5v�P�s-�E��4���q�      4   F   x�ɹ�0�Z&0�9�%���9�q���D����#-ߥJ۵z�,}E�]�A�\K���m|�K0      6   9   x�-ʱ  ��Ɂ(a��4�/^� Q�2�I��8π��F��'W#��q
�      7   �  x��Umn�0�m�"?�����e��ǊJ��ڈ�@r����ؤ�:V���y3���x���������q�����tT%��.j:�qG~A��T����F��NT�'�wX�mB;�����8Ծ�t�P��Rs���e�>@���,���R�1���n���C���i���d���vC; ���1��r����z��B��L"��gFћh�.�'|�4sD���αѳ�²^x# ���Ũ�(�JX�X��n�|ᖳ�+��  �-��rs�ޢx���K5D4���&�[: GÊ�7k�ߋ����l�\��VIH�~�r���HMn���MH��V%O��1��ԋ�
�ҋS�"h�����s�����N�X&'��rW$S{p��i��e��u�`���ᤍ8۵�Z�X����	�ѓg{�S��w�w��]vO�i��(:v��ʃ���,��Z���C��Ն�;x��'�3���(����P�\��5[å��]�L	C玫�L�>&�����͕>��u��?ns6X���:��?_v�=�ً�D�fԠ�I�g���n��8�nXE� Hp�oO/�O�`����+F���@f�YB��e��h\w��<kp���
9:��bL��2���M�M32)�X���awT�k���m��8�46~N��� 3N�*      9   i   x�%��D!E�5������hxn�r��C7Y�Z�Ww��
/�~b3Q
�BWA�'>�|򎙚�]lI!�`�*3̒�y�5�X[�t����Zsm�6�~���_"N      :   �  x�}�Kr�0 �59LF @p���EFn#WԓM�"Y�gE`��f�ݨ9#e��"Q�1���G˿���!>��~-�����K`�Ua�T!���<إ���F�lS���Kdj�����P�񹳧vp��!6��,U@��?����V� �� �2Vj �T��w���
G��{�4��Q�Z�n.}d(s�R��]O����\� �Zl����Zk�m.�Gi��Ep��@��[�x��}J��%J,m�F���(�Y6�xhψ"6�:���r��f�����e�Di��yWJ�5��~q*�b�o�1U�����D� q_y�v�$��v'����|�	+�G��"|�1�7�kN�S�c._#㌼�{D�����.�G�[v'>�9�q��zԘ7Ww��مRΪB{Nu�+���⬒3��=b|=oS���?X��_o�����z����U�      <      x�-���@��o��
�9��^,Ɵ�	��V@�%x��LG�`6;��f�����	3&m���%	=��ƋR���z�a�Xe�->���R��\؇����#��c�x����2�Zxd��}b�|�<����~^�      =   (   x���4��4bS.S 6�9����M�ؔ+F��� kB      ?      x������ � �      @      x������ � �      A      x������ � �      B   R  x����J�@F�3O�'��$M⺂(bE7�Tp�Ɲ�-(Hҽ����F���y#��lJ+H�L�=��;	�MF�b���1��+�_M� ���RI�2���?E󓤈e$�B��<�;h�af:�<t�×�xr�N�-	�"��XV^�M˴	���Q�ӦPn��2�4-QR�(�,���BG�B�B<xF��3!�i�+��8>?�Ֆ��R�O��_�/�H��@�u9v:���kV��*3{�W7+oK�v�oP`�͍Ƅ��_�T�C�ܴ�8e�F�Jm�$�*�#^*�_?�>��q����6��f�~0W�	ODmg��E�IEJ��	0�      E      x���rו.��z�Tt�	 ��	��'(��(�EP�oG�)E�L�S(H�a�,�)��D�j�"E��OE %�b�� ����^��kY i��'�v�5d�������C���j�*�+ڽ�޲��1�|Tt{���N�}h|t�������ыc�SccS�c/�N��:~������c�~���,6��+?,�*Z�{{E'+�ͯ˽��ڽ��Xϊ'x�us���^�-v�b�|`����F�*?���F~X<,���>�k=1�}b�g>�����;E7+�(��K�Ϯ����+rϝ��*��她��fe�Z��fȝl��+�_xuz�<@f��)6�	̏��汲����ߧ;�O<6#3��@ͯ�vo�=�r�x����-��ن���{.g�Lkp�b��\17�7�z�[3O���ަg��qa��Oz�0���\g
�Ђ!�d�$���,ef2�J;�)�)d��Ń⶚���fzAe�t�i��qR�f��;���_����_:�Y�q���3le���)�*���js��ꚷa����pqϼ�^����x>�u�*2;�b7��Ҝ�1W��y�.�m�2�L|�6�l�l��2�w�z��|����.F�#�zt���6AD�q�.�nFO�������0l�-����H�ڃK�a�}G�Ԅ|�"���Ri�f��jf��MƷ���*n`8n渘m�2��	�%C��E�~�e�4݆]lv'~�ܖ]�����	�	�n��ޘe�{�H'a���u+�b@oWX�'(�e9aw�av���E�Iz�ʦ�K"mdsyZ <�;$ z�r��|Y��aݗqr���˼���\��I��ޟ�Nٳg6at���L�3������9�*��ψ��ݞ�ѓ�zSeF�;��n�S����[ٌ��w�<��m���@�����c�ծ����c�� %����"U�?�t~ƅ瑿����=��k�OH�<�5�}R6��>�'��G/D:�	N�n���>�W�p?������M����>d��Rm����[�0�<�D>)�����V�M�!�ê��*j�'.;*��\�ll_�r���}`>�	c�g\�2K��ډ��y��-�l��ZN�Ij��X,�[�H:U9H\�e�d(�Ѧ0#���8	�u����I�g�Xٝo����dSt'�$�w�V�M�c��4�2��$���.Y����?����}�!3̓*f,�8C����!4N"�1���e[�������fc?�s��V�d���Ҙ�W#�q��q�(7ADfx���M3�d����0[`o��C��V���")'� x\}m�	4�Umf��3
{-8���e���)d����0��û0�_�O��k�F���њ
��o��p��mt�gŧ�p���[��5n{ޒ �����M��®��-��)��_�Gi�p�P/�K�VnӖ�A-���gx8���y�u�Vyf��s`P��F?�g���zG���l�4!̥��Qw�6�o��]
R���B�f�{l����M6���^�G���X����{&v�C�u"���Q̆��r�F�j}>�^��S��i�� )̙�WB!ʖ�����.�~Ư ���'$�x�ױ�K��ŵ�k��Q�0_��F>�����R��ף�-3G��^�������Ѷ�%�#c~���3��Ya3Ϲ�P%UH�mr��W.^<�w����<��˵�R��>KY�g�� ����E�>:5~L���&GǏx����O����u�֨,��j��ߗ?W�p��J�f��l�W.]��\Unr9�(������`>���zwh���t}G�+��7�R^��]R��=Н�h&���W��h�Qc��=��(y��[Q��G����	�~��5b7��ङ�<��8
xO��o���\'������8B_t���$K��X���E?h1�f��k�u��D�g�0�H�j����ƴ���m%��*��8�������p<���5Y���]�8��N�������oV��bu�u�Zfl�ᢅ��6I�m�G�Z�9���OȞí�B����{r�r�a{��ڎ�ʺ�,���]\�5\�-�X�#R2�_zr��;d*�$�j��O��<��+K�+�٩�B�Y���U�+W�׫��A5rlv�Z+3s3o$O/��̅@�}H.Y�s���v���*_����O��~�g�Ys��Gg!w�����;�����a� �e��C�l����S綝�	dݵK���:\<>�t�C� �ID�^���qS�KO��-D�;z��޾M;�����$X���C%�ƩM2�,�G��
����f�������4�@"�yR�V�+6�Yiܴ��z����4����Wŝ��Z��N�߽�K��o�|�9���!|^Ċa..d��������n�����6�i�u��XxqEܞ�p�c���Ql.����Kvq�?qd�G��V����8���[����5ʞ�^r@i�HW�^&�v�ϭh�p׈s��<J�^�a��,�LN��X�O ��;Da�=>��{��KV�!0�/����-��)�s�&
�Pq��%�}��O�$Pt�+t���KO�p����p����5�u�m�?�M}�쀻��Ϳ1�R�s ��������-�f�S��7m�� ��S�A-��b������l�&l��݈[zS_����۬����BN�ϼp��+#F��.�<޹�Z|ʤի%);J�����Ӫ-r����J߷Y.|*�*Tj�s�)�'�$	�ά�+I]'c��F���Y��N��&��m��R �7)sO݁�U1�g����Ք
�~j����*�n�;����1�}n^���Csd�?�s�m�W�:#b*�6���l2�޸p.�^�]�6�+�j�ve�6[i�yv�~}�>o��Re�:��u6{�Q�^}�޸�g��5_k�N.Tf�V�yv�:g��i�ީ�{3��\=o!�ŃUx�;p�;������\}�ּa��P"S��Ņ�����7��Y�b����O^7?^jT���V慗OW�\0����E�ܶ�[k�PtaJd�>����D��d6%�֚�^5Np6��0�2��P�^4G%ȹ��W�*�̇�UީB��:�6���7�,�¼=]��4�\�T�wH����;�;|(q�l4m����(ZH)���%������=}�dtB�	��ټ��;S���b�J�:��Y�sә�7��6ީz+p�O�Ӵ,t&�7h\��*�*�'��p�l��SB�1��s��L��k���_�L 	)��}��ʮG;�e>j����{606�B���UV2+i{��-΃Ļ�{!���5���}�����I"���m�u04����G���E�iE���)�ɮo����myzV��Nxѿ6:�4-�����C��hEJ�mmNZx�D��PV-�<� }����$����O_6�b�K4�I�i��AㄊѶ	��<�B	-��q���䳷M.g�KH$hQ2�Ð=�>��U&k�#���l������qcbve���:Ʒ��ň�0���[�V[�y�Kv��+(��`����J2�ɛ���!9��M�A�p7�b��	vSw���DI�1���?�8�ձˇ��T�:�����O�����&��=v���N2�e ��h�L�7�Q�=2s��̆�W�d?g�D��?�2:#�lm�y�bm�E�� � b�7�u�s�>.�!,�ؑ�cG�&�OM����c��'�bh�z����K�zP�W��aڃi�g�j��d�����(���т�m�k�hd����0bl:���A+��qyU��Bg$x®��¿��I��5_�����];��Sp����GZ�=�%�Y���Xg��,~�߫�"Jt4�� :\Ewș\�5����fb�����$���K��"�R�E3��L���5���?�X[LRb2*2�Z��׳�ѱI����0r�sE}�j��|F�$�U�9%j�[���%U�G�ku�kbzm���d��l�f��;oԃT�� �~�Ab���7^吞��Y�o���ܣD%&,a����a6:�t�n�E�p�    	��jG���@"ڕ

�0I�c�fβ��@U������4�]U�zϦq�g�bvr���DIl�$�.{$�`_�|�ax��_p�3�Éw�F�8UE�&4�W��e��PR����8��b�q�	^�M�}cgOeW�ͅ����j�gW+�T���s�����q�
����.K��aC�b�Ա���W{#x�k��#��7p�����p�Q^�ܡ�
p�z��v\�������=KI*���۸|�b/36ˊ]u<����Ow�C@>=�Wsp�R��.���/F_m�7�_�[ʂ[�*��)�s��T�#�-��0�=�'\�yˢNX�)I���'�[�Q�ݭ��sB�>��a@�Q�Y�V>ڭ�R�k��Wb酂¯RZ�;�h�������������k�SY
�A��D�`K����&bD<�Jv��A8$�h#X�j�9�lS{�HYw~��˰WV�/��>�ddUkW���+��Oy�S����F�:?sc8�r��Z��jsUX��x��0*��O���h0�;���}�.���3ѲJ)M�E�����=���ޘ99?ۨ�pab쐷��:{�+'��J�|��j����ۧr���s��,�KFP4���g/�:#�eF��ϟ�.T+3�.Tg��y{����｜�Q�{����Y�-N8���R�q#W1
w`����S��y�b��4Ҷ��g/�U߻T�4D���7�|�%���Rb�G�7F�6��fr����A�Tr���F��
���$�dyk����c͹�`'o2c3����h$/�e�7�%rV�Aj�>_<�L���oѴ$�f�\,��ou�L�-kA�>��p�:�T!���Wb�1Zj#w���<� ����*��0�Gh"1�P��'�iW|�a+0�=�=E ��Q�h��9�G�ueAx�r1�Ny��*�"-�'�u�����_}gc���:Ȍwh«�8��-��W���XGq��� 0H��·J|�dj8�C����s�p� �*VBZ���s������:X�T�j���U�(j�&"�A�ߕ[q}�kh-+[��V*�78�6�>�g�t�M���,*�'U_� �{v�~% ;E��J�$���젦�?<ǈ޻_<8���-VpKRl��J(#��l�� e:�FN��B����ܫ#��$y6�n����zs�6"�W�a���V�g�t�5��L�Bj���6i��}�]q�l��^��o�ٮ��u�P��r�[���}qp�lT�ư�"��o��-������Z��9D�](���/�ZH�r���Ƨ&&%E=~���	�ka�����Ze%s<%2��ʱ �r��`�C�Z oVFC�t���p�V�G�X��#�f$�Dz�1���6XA5�_Q�=B�[0b�ZB"�&YG�)y���+T) �k���R�:��p�C�z�6��W!K��/N��L\��x�)���3��{	
hܨ�<�h9:�Vp+W�=4C�T����a��e��~A{hmp,����T�]{v&-���{dg�ʚ`(��"��n%�s�:su�d��M ��-7KT��r�~e��g'�W~V����;��j]�o6�����0�00{�典=-.Ρ`�r_�����]� ׵�4>�]�Q[W���t<о���eb �g��(m��å���ꇮu)k�Q��$y��$��j۝Lz��!���DH����SU�
꧁!)��q&�A1&
Jʿ&R:�������?]�ϽXk������g��d�4a��x:��o����d �z�R�{�����p�xE�����M����;��B���)��ʚvERR��K�a���s�G��Y���\k^]���#��!�� ����`����*��3M�Ʌ��'������&%�ϥ��F`���!-r�^�c��	K�8����N_������gO�Z��II�}�Ҳ<|��Fp'Qߡ�Z�����v�:���38��8:��#z(�)���|(��d7�Q>i�`�"^n�r3в���F��r�����V�Z�̃(E��y�r��:ض�m7��8U���bcX��[��wq�q����3q\��؀o|��U�%�w���TZ��W�����c,���h��p�M���c{�mAxC��sh�x8(0�OB0����M�C�7�WDe��b� �CT��fuё��x6<���F�i[,4�S	*ܕÕ��E�c̄�2�jT�zI��&tm��F��!��T���3񅸀�a��^�;:5965:n��c��{�i	��C �o�؁�h������z����P��7��\Aϙ��賹��{%�AZ���G���[+�����E�Lzد?p�F��C}���7Ɏ�zv��s��I3���_]�~����5^v�(T_�U��'=a��^���nr�~|n:(xWEH����-<[��"_�%	�z�?�O��Tzjo~=���S�2?66a�������eߋ��v.�O8�/Ϧ����}/���M�G�*���R��^�֥?�M+a���
��7Ų!��
���^��\�9�ۭ��O��;o�!�@i�7/1��_r�0������,���[J@�䃢G&��YD}b�������YB�VA���b��}u�
8o/R�gX���

��5!��ӵZ�;�&��)����#�m595z|jl������ѱ�GT��/�]�����E�)6vqҽ��p<�Q΀��Bղ �4�؏|��M$����:~�Z���O��ȁ�-g)oC��p�B���?wC����Ѕ��S�r�c����ng�>Q&0g!��8-.�?_Ffo��ޓ	�Z�%��C����Lb�?$���3�F�d66��`�����F�r=�,�R��&��RȜ2�����H�5��;;7Eܬuy�G�w�^vT�St�v��Uë�{Vm=���H�����B��-��V�6������\5����{>}�x��^�_z/
���
(�1 "�>y��9�|y�ۜ�pa�^�'B|	IAR�e�aQ���|LSj�˱�9@���ܭ'��K���sY*�7l\ޙD2���0
�d}��E�ə��r��Wq(��s��6�S�rS��(4�H58h|pl'md	y3$<z�!�u��_�I6������lh��I֊G�Dl4<�17~�q���� �v�^����K�DQP�g�22i�t�%�A5�T/�k�閶NϹ ����"�}�0�n�e��"&�e��zP����^������YSvE �л����s��:�o��c�q���:<jC*�G9�,&Y���=v��M�A6�p<�����0�qKkߌ"@��Y̓p�qߑÞ+_��Ӆ�U�0�^q��:6M�B��N3m/��R���e{p��Dp��	�J�Y����o���t�÷���H�dM�\!Eѵ��J(��k�l�?�,v����UX� 5kրs�C¦bD��Yk��cϦ�g�_<�cF�H@��O	ƷTb7�V	��cgޛ�Ν�'Y\*�(��˳������ve�,���(��D�HH��x��Ӂ)ӫ��J�ݑюV;�<5��,$�(����h?�I8E�fďqa�讂0b���Y�42���LBH����ɢIm��+ǖ�d*%��Cb��	JE]�$+xw�P[��7B �+�R`|�b�ۺ3�xF�乛V� tv��L5�行f[l�u
H��|l���}�QV�rȃ��%|��Lx�\ʵ��C��(R�&���<h�#	��2U4�^Iq�����x;&�Pq�7�IT���z���m|6J8 �񹱱~t�A[�ɣS�Gl$f���c����>O+���;YQJׂ4�V8$�<<��/I�LPJ6�Jk��vȫ:� $�;l�u\�����չ�Yx-B�����vݘ�%�Ί��ѝ/Zt�dޒQX�UOe�����8������ZO���t�c2�-%�}�G�F� +�;`�f)s��r����6
��@��GOh�G�r�܋b0I�k���*}��u�=	\׹��-�{�����h`�J�C9*�hD=��~j��4;(    �V� �qn���Dg�Mxo]����U�����B.f���n�c�[���~��!,���|�Q�ޫ�͏�?�����Z��'��[�0�<[�@��G�Y$�(�#�*��?c�/T.]�5Ͻ9��}�\�����4<���w� n`�	�AaKZ�}��&���幥��LN,gB?'�ez�u
��M�ɷx׬K�renn0�����Wpg2�]D$����Wj)��|�����¢zaz��t}097e��^*�Z�JP�R�8%1SW/�'���	�\m�����t��a����"���bܯ�c���eh���� ��ǡ
��?��}�OQص�� ���}a��ɤ:.�[�%*FX�g� =A*�7�,ke��@�|��-��.��͞fO�����"l(���H3m6�q��l���'��3�Wj��j�Y��ǎL��:,�i��<|���\޻F9��e�#�9&�"�Hc�I����1�T���r�n�����_Bὁ
Ϛ���@�ӌ:�h��c�}�8��$h��;u���2NK����w��O+���qq�8�3�'˕<�58!���{4�mr��B)fS��q�P;���I./��x��%�T��r�N����˜�=�z`�R�֡H���Y���� fql��B��T��ք�U�e�-���t}�Z�r�-��TmV�&��\ʨm�v��0ۘT�	o��>[}uz8;9�X��Z8�Xm4*����ٙ�_˳�k3�z�V9Wm6j3�¯ׯ,6+�W�����j�jui��˕ys��c�^N�P����x�֬V���xeiJI��M��8Wi֯�͗�7f��k�/�T_���k�����E{j!��rhy��r�5�Ĳ 0�F��(-��D�o�w1��B��&Ae�8���e6p�\�1k'��t�U���o�U�`�::�һ�͑��=��fq�����N�H6
����e��Uy��gn�װ~ȥ<�(�e[�r.Ζ��S����C
i��)K��x2,�!c���>ܰ�)u��R��b���g��[�Bec��rpڪ�=��eB����~ZN��������{>(����eS	��l0Τ"���c�&n�$����n�l
 �QZ5��U"5����N�\;��K�[l�F��p���=�mӔ�ْw"��}Q%sr��IgJ�{�$0:|�ml�B���Y��� �@x������I� ؇��<�h��2_��0	�$JT̗NW/�*�~r[��e�YF�o3عE��:|t\�![�+�ͥP� =K!x>Q�R��Tm����Y�C��r�]J�^O�9{R���a�*�� ޏ'O���M���:o4����hx{����0vB��O���l`�D͙�٥�yV3�;��)ƺh6*3�F}�Y��R�OD@%:~�xx��Df:�\5>����ܜ�w�,��,������\nmLc$6�c�6��.hr������7���xR��$J}�|�w-}~���#k&��^�c�3��9�Je�Q�F�a�/,%{�=$�6����ࠑ�/�2��/-,T�[�P���~@����U`�|��ȩ�f��Z�`0(Z�Kd�Ԛ�|S#��K�����	�����z�]����U���I,1������K���;�K��Z�Fɴ9�gD�����v�FN�UM�dٰ��.�ya����^�]�����,6�5������Fޮ�-�5���RUW�L��W������bk�֤WM��v�����+.���Įcq����5��#�����\�nv�r��a�5�=�Q3u��Lv!�F�'�ڈ��$��<�� �3?��l\��2�:�#�ە��|�Ed'i������@�ȽR��>��L_x�!�����������̵��%8�*�����D��53���Fe�Y�ϥ2%�,�U"G�5eil��m��so>�]��Um�z�j�������u��=][���Sm܀��ť����v)2�|��R�$�]cAl"���M+Q:����4��I)�*�[*�E+�X�E����5c.=ζ�)zO{}�����`6��CЗ�-.�B4[Ks������3�Wn��q���������m�5 ;����Ba3'�%�0
K�Ca�Ǻ ߵ�MFÄ��O.��
�]��ϙ;���;{+���v '��\$��z-�)T�u\� GI���?��:%�>I�q�8�4�ש,�Kۊ1��7���'�5��=��q�O��O)E�s��`�_F�E���a�\r\x�,��TK��d�������Gm�9��I M���X��<�Dmħ��~vZP2��#Q*~7c���Jm�-`5=<L3�
�b�PW���x��D�{1@�%[h�F2F���̼1=�����n�?�~xtth�����	� �p����
A�BEcԠ:
��c�����pY�,3q��GY���&b�&����YOzB�����-����AߓEG��ڸc�xU	�|����`�gj8�O����0n�>�QmX���z��2a�н�� ����Q{�8��@Q�1�B=H�N!r1b�k��g��ٞ�nMe��ߧ$�l��pk�+�l�j6�%W�P�.�˭�����:N�ڀ�E)�ȱ2��y�3ʹİ���N��7~����!�H�W�gǑξ��[��*�w���_Z��?ʞ��!&ӳ�5Y@#��m#����9���Uq����?�W����k�WŤ@\>�����AXv�c�s�軔��$��fl1i#�u�6SPX��c���aΎ�\��O&/�9�JD��Ϧ�8��Y�����W�{!�DL��<*vbB��@�:W_����Gpll��a�=~��ѱC�J}�/��0��0_cy,�RA���0x�S�ÂM���3g�
���YǙ�i�[9�o��9S�z�h�7?�SO�m����չfe�a�f��34aO?�7�V�Re̽.��Cx8��U7�ag�����3G�dZ���(G� S��E����'pP�>��#ͱS9�'y$�!uQ�F	[J<U�l�uO&�Ӫ�S���0�X�b��O_i�q�pn���u��{�!JˢG��P�3�I�,�1]^7k��\/`���,����%W�@���f�x�]|���]����K�4K��R�D.��Z���h� ���e� ��ý��_����]~���,5jM?����䶭j*�Ŭ�{�O��Ɏ��R
�]c�FBA9��z�m�d;��E��aL[h#g7�Ƹ�_�+��0a�RUpG��T~�(���Tf��X�W^�,V�y��W�J�FXZ������������.J�QM#�h�(�wn�YtF��d����,[�����+7�� r|�]83}�D��:�dhĽ/ѩ�O7�Q�(0��zu:�/X+�M�H*e�=�k���k���Qjp��9��f4Oټz�zu�Z����]"J�I`̠��(�3đ�����
˞�f�n/&".�X3����Re�(�� k��3K������ii-˄��h��̵l��G#��W�^8��Q��Q���� �^�S����5�g�y�:!C��ףH�c��uU�F>A�b��./��e��޻n�ׂZwt�����>�VE۳��?�b����J��/�6H1C��x"�Zƥ��qa�$�xc��#�/3cp2��#0c{X5/��JNX?�+E��d��aa+�������}*�r��j����*Еq��4t���g@����X�����#,"�ɀq�rF��6�y��8-_���#��R�����`��^��`6c�
.�r����n�BH�Y���y����9a��ڻ�ޙ��TC���qSsy r+��1��IӮ�Ѩ�y�е򋧟v�!�g�+w9�K�<kOB�H�'�ߑ{�;��\$��Vp*������[�c����P��c�$u�_;�(���f%L�]&bEwG�z�mp�I��n3՜�a�&+5�"?�<��Z�<��|抒DI*��x�MW���O�'|c�*t�P��Xĝr�13�i����#3O�ڧL�'������n��;(�    ܲ,���9�oY���/�n���}�-�`蹱�O��31zq��1�
���G&�B���Fmv�6_ͦo,6��3�2�~�% K|�K�G��~]x=PW�Q��>-�ރ2sKB�������-cC�t�q��(��1�E��N�
�g�'2�_�\]i/�zČ��J<I��\S[
���zS���&��$�FG)XGP�U*��g={q�a@1S͉�-������b�9�����lNʀ2�Z;�4ľG��k{UA,�m8��>[MӃh��m�&:b#v��ն,���M���?�s�P�!�fy�H+?��3�L9�E�V��:��
�o��.�~j��hqu�5�����e��,S�;!�� 
n���r��vf�$�.���Wgr�����heX�mٹ���;)�i���~l��bZ�3Թ�L,]�qK2;����*�N�j�f>��tzLe��fŸ��l@3aX��Y�w���EB��OAX:��j��U�+h�__�kֆ�W��,�PY�1?s՘y���l�Q7�^�7਽�����yn�t���va�V�w;�$�HH��M��ڐm"�[���ʦ�.�Sm,��7C�	KFq���\E�ʸ��w>n��f'@
�d��L9�O
����o��3�
h�D�Tr����0O`�8^���3��$�V[���.�V	黈B�����q���	ؓ>tn"˘��^qE���B.�ӄZJ��e��+��R�L6���H�3��N'54Z���9��Ew��[�N�a>(�S�����8���	������I����)�T�ބ��*������1��2�9@ty�u<uL��(��(V��V�'d�ӽ$ �B��B\J�����Ɉ$5�O�w H澼������E��>v�u�?�u�T���!�ҿ�;��7O����|�d��8�A��t����F�i�M�}�>wc�>[BB5�YS�q�X�-}�(�[V��v���k�	Z�Ï��Y�4E؆ #��ג�JһV���@���fT��Ģ�\�5�Np�e��x�6C$F���\rV5�q�.���w�8XIД��Zb8S2�J1� Sim�Q� �mOZ�(ۀg��:�
fCy��B+�/�rU��ܔ,κ'x�0'H{�N�9=Ye�Y�����%���슊���$v�9��>躽�k1�ʳ���c
9�Ծu��v���+!)e��0B fH JE�d�[	鞙���'�����jÈVn�j��՘�K�i��`���>�-A�)J��$ҵ�����IFY{lW���i��"G���2j�2��R��ư�5_wY�����b[�/ C��[*�m�|�H��m�zM=�kf���hL�#iB?�r#�V@z-	�1#�z+�(	����0�*�m2˂��\9�*���X`)�>At�G�T������P�6Vi��i��h` �~�"J���	Z5��w��:~l��m�=>66:q�/���<�=����w��y��*.�.=%�^F�J�xw
��r�'�ϭD�N%����S�L��l�R���{a+:,ѵܒqa�=��B����$4I�JufuOr��\X��R���+sՑ3�o���̥��9�oo���׏���}��o=���ԇ�Vx�W���%{�g�]WvyB6��J��b*��3�G_B�Hځ�����z�C;��������b��.�n.�3�p��_$<�ώ_�Ily٬��a�8��eTi�*��g��+�J��px�/�2�mPŨ�pE�j׸��0;�;��S�rӕ<�������N8P�O-�k���<�:�,��r�7+:Qt)*r,}y�+X�3�������3/_8y����ȴϱg��w�������2�����6�� �̻�7|j>������?�<�ze���Ͽ��@Lż-���m��3\�3�����ZB�t,�k~�����-|(Ï�ڜ��ů̫��s.�
b�Snst��O� wͣ܇k}m��;���g�������2��KO�ms�v?6�K�Fg'�Q#�A��
���4�w�.
��pl���P�P��}!Y"1�r�G:��X�o����"�^y}�b�J������v����ka���5Q�yi�|n~~j��8h�����^3;�rB���hE�f�	��uǹ�v:�8�TI �Fv4�V�ċh�i~_�����!�g��6n�ܮ��]�l�Z���}cTe혂y�9�����}ֶ=�_ @(��Y�q<x�k�M��k�v�L	5�&��ԋ[a���a0�G�����a���I�Ui��+�_�TD���%_�������S�>�'Y�>Q�uW�(����4H7>:� ���7���\�kҢC��ί�xȗ��S����+���"�������;�K%�T�C܁>��q�����y�G8�|M�AIbd�4�����l�$ߌ�.!��yÂ}�����q�a�rL���1����F�L!�/�@�p0��$��u�	�1� x:�A���[�͓�l`��ؼ^���F+�����sH�e��s����u�ҩ���뗪}s���%:��@}����fVY\\jTgKE�}0��z���24�@��nq�@=��%0Av$B,�m�f����@a{�q\�m!����?�*���cY��21xAq�LЩ�)*dupYH;&ut���R�A�-/�=���4�9�d+ԍ
=O'D_�&��#�	"q�͌ x�#AID�Nf.���ql{�E�?5���΂�(�2O���J�GqW{l�և�A��A8^�E��q��ӠÊc����i�j
wP2���pC�-��#�k2�������k�ӱT�,�7��	����.'��F!;��D����~��F����u��>F�n3#��r�.�S�oF���&0�y9��}l�5�-:�ߠ�m���o��{ͿA�,� _Y��>y��`��E�R|ӿQ$���-?4ߢk���||�n��4����wТ�ϤCM��%��_�����G&�6�)�B�4����$e�QԮJ=v��)��ӫ�l�t��\�d'�d��]c�k�e�%{Iv����Ҡ��·���k*I���z��ު ��i�֔����~�\�u�1j���=JeH�p-B���Ȝp���Ch� 9��q�y��!�t�^�4p�� �� E��y/3]s�&�:�X�G�r���ʇ�1�1�q1�`hl4A�`{Tu�rZ��։QlTĆ�Q�t��$�_��[�I㸏��R5�V����ƴv�2̆��])�	�	�+%�^`���q�|��I�3���ǝF݂�Y� �=���%lb�������ef�$�A�<ױ��dݎx=>�Pȑ�cQ���;y\:ա`�k���yB @^j�����/�F��K".��}G���� ڍd"��^�o�C�>"*Û�G�1���1�?�X�S0mЫI�� ��I|߼Re0ȈW�295~DS�?299q���w�}�'c����<p�a���Gv��wN^Z���n@K��L�z�����-,�s����&=�`uwli�V`�;�G�����z	k�;hMP!��a�QfB�b��� �;�$��:�ȵ�ѱ#,�zk��ts��|���t�w?bFBGt�a��%kHE��� ������Q`#�vfȤf�UNМ*1�=U� u��"x*<n��r	-cI�E-�$����Aak����1ڮ�,~0qS�}�4&���	֟��)��z�(�4�L�o,�<�U�%*�ߕ�&�щ��
<T-I�����-��i&������ 2��19O�E��
��_r%krcL�}w�<�hm,n\הa�p��P�Zm4_F9d�f�DMt�T>*�0\=��E[x�1���}��^ Բ˯�Z��#����k\�0���wq��!Y+������mx(@�1e'B���K�9Zb#�b�3�	�詖OD��(�Q�=�kc!i�5����쮴/N:N�-\'pbH�~%�c&&զ���P	�v�����8L�1��!/4
\���QA&�2�m��:     �.
���+��mX������)��ߣϦ<�=�4��w���U��%X�(,����\_8�]��\3���	8��Ĉ������N&���"Z-%�A<%{½(��mL�$'<�!X�!��[�H|%N�
KrO��t,�Mׅ����]QR�%\C6դyJc����G�J1l@d��9�ƐD����_�=e�Mau`��rz1����#?yj_]��G�-Z��g��>�h�����S��s�4e���Θ �EQQ{�v�{,�2���0&�m�Y�)�lB;�,Z��F1[�!��Nr}�K��WFu�Ƕ�o�R ��5�8�:q5v�����vi�m�9e#��:9%=�xg�D�����5֞�ڒ�݃P�A�a�y�N�z�,���$���Q�	��.B.��|�d�fI[�}�ځȖ��fH�c|�~n�O�M���f*N�u�[tU'�B�
�^����Ǩ�r.�c>�X>e�<��.|�������$��!w��`Z$D��AJ�36���k��MZ7QX���H럒����I���/��<k�4�D���3���X���}�q���p�-��6OS�RjϔM���gj�=��N�Ԛ�w���,3)�(I�Ti6�j��3�Mi��9��v���\}b��h���5?N����߉#�_U�����a�-(Fn��M�"IGu)e�7'ɘ�����]�b����!��}�|3ـ�8��ޑ�,�BN �>��$V?�TQ��J&
�Jq��}i�q���U�AlȎū|")��Y�����~dhl�׏��Ǐ���E4c��c� ��=�s+/�������%@=��F��4�:���0G��ZX$�s��ytGI���>{"T���t��&��yH�g�De� {s�N�?r��q:A������G ,����w%`��(��k��T��f��L���x3�E* KzDv,FF�ږ
ac�d��3��)�{�#hg��sp�0���e�F;�d���Y#5�J�[Z�/�q��k�9� �E��,���(K�o�us*;.�^<�;fT3����{�d��(���"�T/ڎ���s��� �EW�byW�A>�V2 U�!-m�-G�։���r�+�n-D]^��Τɒ"�)�I���=��
�b�.~M��:D��lޟ��)��H#_�=�L�7�Ȣ��$���~Gʞ�:�Z��Rz�����%.�+�ByL��^o&/-Vه�Y�b�(�h(LQ��Fu�.�$/�CD(�N�U�87W�i���yi~f�Ѩ���,c
���F�z��z�Z6p��Sf.�{@���,������]�_1��d�����FB��`OY1��O��T�t�tT)��=v�ܜ4H��!�'�l���X��B2�2v ԃ(�<̂�b��mk�J��A��~��؇R�|9߁�t7���UI��4AT�y�70~�KP��r*�nT�P����r-<F�'�vsx:*�V�ڄ�At�����Ӷ��������ȹ���BJ���z�/��f?�8"GQej�z�-����V6@]�r�5��3�B�C���AX�r�SS0Ÿ��;�	�I��ׁgۀ���8d������4��nU@�+��eԮLJ�ä�U���ӄ���6��ʍ�~��j����I8�Y��#�Oٜ'.��G��<%�Z� $�L���ݦ}���N�I�����k:{�{v&-� }����
89\���Q���'NG�5`ZӀy��LJ�� ���G�,��R�p�7��ٳ� n'�s&9m�s0���	����J��N��[I�x�P�g�{��M�MSGQ�RGȎ:�	�#Y��r��`g��Q	����m�k��l��8v|jb|j��v��O�N=|H��%f�!w��)ץ*�*M�ڱX2����"�vl"!��ή$�b7θ^(Nj��n�*^�lW8`)n����(i�Ѷ	)ǰ
IC�L5����3���@�:4ۨ�S���.�D**���V�[{(�c]�/�pީ��zs�6�g��>o���{7����s}�=��'��oԗ��y�/�^��g������ʕ+`6]���]��Ko\{��\ȳ�?[2>���;o,,�;�<�*�enu��s�a�� =o��A�M8�UU0��� G����<o���G�d�S�c�3֌&S�>,ܑ�����W��t�ځa�>{__W�zl�v6}�������	��� I�di�l4m����s)�OX*|Xج������������k�w+�B/�@;4p�B�� Pֽ'��	$��p��!���xބ&B����2Y�`O�:�1$9�Bߧ�PqcQ�{���+�_VV]Y��FZZ����;�31����T.ԗcЁ��n�Ux��ԻҔ.��mM�CS-&bZǄ��P�-��	a�xY7_�+a4K�����=������{7FB�>�B���]>3q�u`Z0F�Z\�_��!����[|n{��M�U�eB�ֆ J]�굶X�]=!`���T���nK�,E�-Բ	��L+bꢔx��:�I,u!֙�2���$�*����$�S�cY����>��uxO��DDǻs�.�N�z��莞;.�m�O��:E=6:����u�[	s�%��N����	Z�c�t�����ؖ��g�#,�̩`O:Dtd2��ȝg��#����Ĳ���5a�W1eRs�����\�tܛ�{��-�Y���������a8I���%�����)F�l���Y~E�R�ޓ�3���h��/Ֆ���/�yD�Ǽl�'\�g;ų�2l�tw!5���P|`�D�r=0!a$dlj�pZ�G
�dO,?z�%2�	mE�u���9�)döi���jMI��a6�op�ovi��;����/F��U��.׿�Q�?�]����H��¥�!��7_�%�gq�ޖpP�y�#r x�}2��8P���	+�*l/SK��`eg���!�ZP��6p�r~I�$ֈKJzx�2�(��T���o�XWl0Nȼ��s
VF�ge�Ņ�f���fZ�����J��+�-n�G�J�K��}cp����	�at{��u���ԏ�\e�j�٬��'ɩ��h�9��j��#�Իw����	�����o}�c�#�����ȡ+,�-�d �Y�!��b:s;8���;��2t�)�Fö8S�ep�UQb�����]�I#�}�;*�k�����ﭖI�`A��^T*��q;-[R �����1���e\��������y�CD�*��J>'O&:V�#��?�r}�2�D�d��^������ۉ�#�G�����_�\����.V+��n�ɶ+�������	(�Ó�/��j�fu�*���N+���5"�'���j/���_��6�x<$�'\X
ܝ�J�6
.wuA�NԀ�f%�Bv�(F��j��������Ti�%�^��"�b�Oŕ����h͐��pq�\�s���JO!"��mu�B�pBn���Gn�!��P��`_��.��v�Uѓd�8���NtCc��>� �����0bv��?��be�������C3Y���#5-�#��Sα�B\q"��%�9U����4��O��oԛ�KK�͋3������/���T�ys��Xi�\��P��i���r�M��	��q�O[ow*���q	1{��t�NT�#��Mdbj��GpSjuy�2;{Cn��k�t�3/V������A��o߉���	2�b�����K�Q8�Z����nI�7��|h�- K��)��Ϩ\��Dkp_�엡�ʒ!r�D0�ϝ�����͏O�t�1��	�?,�/%v����4�½i��1X���Z��ܢ&f��u���	�Sʀ�]��;�a�������elO:��6%�C� ʲr��UX��nj��L��uy7��L�6�i�q^�v����K����} Ā�bD��"jn�m7m˿��թ		��ƻ怴��Ù���9��7.�S��]h�֌�p�u�i� B�Z	��/2֟.J)��)�®�*���2_v%��B���[��Ù7z	��p����    ȫ�Q ����2�&V>���%1�,��f���¬��CXȀ�����	ڬ�4߬]�f?��VGNW��#/.�+����汦ͳ�݃N��W��W���A{$�J>��j�VQxyk%F���pK.L��C%3l�Q��.XMW�rDd�x�F�Vfn$���(r�R�E�>C�s��͓�5��j���IU����tݾ_=>5165�}�N;|ث 83{�Z���aoY�y�"�7�p�X��NV5�=P�@����'��"#��]�g#x	¤�C���܁�=�Y�X ��z�).��t�[�����q�\
�b}�T+v�)�������侠�<_�&�r����N��YjTk��b�2[5Zci~�6���[��{+B3��U����C61�(^���]����e�&��bS{J�2��
*4Ɨ���2�Qͤb��Ա���L(.hG<C�� F91}��kqI$��R49�+��C�	�P��9:D�>b0��\��6�� ��5i�eA�g�-�V��ې��cR�Jl��x:�i�(*�Ԧ�{ޚ�k��#�yQrA�Bg��U5Av6�G�Ps
�gr[�6)<����͋�`J�^�����Ii�qhp�T�FG�~D���ǋF�R��'ʹ�Cԧ�_�7�K���L�W�U�Ն�u���v;���� <�h6uߨ��Xiiv�9���2��l�ca^7O���gg�/�-���/7*�+�tH?��k�K�%��%�*�<؞jk(�O4N�ٙBs�ȴ�#��^�3�)�S�!�x~�89@V=Z��{��fB ���A�,���,m'd�-�Na_�P)���lnȕ�|� \���C�����ς�q��P`�Xl��;�����������p��Ȧ';9��C��X��1����t͎��Ԙ�5�����\Ot.��t���($��O�����Rt���w8е�$���ӍSi$Ʌ�� �5T�a �֦���!���k9T$ّ�@Ղ	"�h'�W��@	��2��3��>��ęU��߰�����)o*�Ǎ���]��ŁFij*̗��Xp�ш5��b?���r�M����:���t#���S�^sSDp��z+��"��.\)��(�
�l���E��?A�47��Ap".u{虿4%�Uύ�IX!��̶I[H�KR�̖�$K��������S�m��#�GO:(o�~�irɼJ�^�ϱ@���Q�F�\CX0+��B0Tf�@FIn���̽Vm*s  ��Bu~��Ԙ�B�Fj�3�t��������z�L�
�V*_A�^2AÞ����v�ʫ-%;��^�$��e(�����V_�p���^P�wk���Ľ%���	
��B�9B�#��]9�DMP߭]��٥��
P���ٯ�F��n���s�D%l߹T���T��#q�۫NUp�m�L���oq�K��: ��V.��I%��Sp��_Vp�F��z;n\X�E˅E����<~d��2���TG��9�HCx]��
,����|���%͟�Dm�mj������@��RP��b��mv��������Fr=[-�D��Ů�-���"�����5M7h�M@1Xzc�� *�0�������[oUA�U!�g�;�R���'t�0��ʁ,�b'���+@,�y�x���wh��<����*ɝ�5�I�:n�
�)�T<:�K�KBY��p.Ϫ��懝D��v�k*�a��z� ��:Ǽ\k�7É�u��$��c"�'`	7��FQe���S˺]T�<W�gc��	��y�v�	�D��U:ڡ*�1&(�,,�uiխU@�~���~IH*G��F�V91�2|��֠��&�5Ĳ��Kss�M# ]���űcS�S�Gl�gr|rll����Zl߶D9m��,�43�	!0a�^Lt��b�P��%���a٢���@l��3FO8+�I���sG|6�������6��F�aTL��,�
Jl�^�k�8��&�M~�&M�!x�ر���Ņ����*��0xO�n�ڢ�c�r8Q��pE�z�rg�<�x�@�zRl��r6�m��fo��x�:e���Y��&?Q����k��c����l�(a�2��TKt�����r�a�Ƒ�k�B�Sy�'Z� �$�R�k��Y��uJ�������<���QԨ�k����͠�O*�Y��,�Pt �}Yuq��}����-CB@M�7��R'�u}.F"��P]��gK�c�^���B�eq�A���á�{�Z9��~B@��:�<
�2�  ����q��4�\Ǎ�-[�4�3D�J�A�S���W��h�/r@�}�r��-է	"/Q���A�<�g�ط9���Ǔ����{��'�:��`����#���$�#��T��K=�����\j<�hܥ�Zi������G�^�(\U�_B��΋�oI
������u@z�'�K�R�\��r��^Y��4�Q;==� �I�Ȍ�o�A��2��"F�&Ǧ����;6q��ѣ��#=�"��Z��P��-�c�&q?9R��\�X��� (�}X4;^?%��e�)W��ݓz�r#+����c�Q��B�C�H^qO<u�B������U���n(Ww]�@@^p����:�����7��KP6���K��RG����h����*���	��T��y�X��wc]��q�"�ln�m΂�(��R��,�U�H�U��Θ#�0gu��b�sAF*3�b��Բ=��E���2 ��\q��Iv�K�$�Lȸ�,k��DY�z�S�e���d;�^����{�q�2��:b���O5�����|v��PkV栶�r�z�:��]&ۘ���>�6Ne�H�^
�G�\�q�D�H���Z�KEǫͽC��-�c�^O���K�k��'!X��M��*d�J���Cg�Qt�#99{��y#%�|�:'��p�7�Y5��~{���;��ʶ�Wy�W§�T�xO%]RCa�>C���g�F���BKB���7J;:����#&�Z�2���=���-@�o����>�6޵ː K���+��X����%f�.1�Q�</*19�m����]\LrMFt�s���8���[�r]H�;h��&�ÂCI�����o H���~wA_��9+�A$�t���U����%Z�%.�ު��leX��'��B�&��Cb^��ٵ��������?/s��(��|m}��U��KI/S�P�1@p�z�U������P����u�|X�����������*�57&��=��l��[D����zc�c���'�$��fk8��r���J�P}��N���߫��G�d_m�=��B	�
"��w w����5���*L�v�d�@���}��j��Z_L�;I�@�J�U�"܎�q��v���@,o�f3�y�����̩c�uz�EhPA�G���.�s~i���f��]s����{��ߘ�>7/~Z�!�!�K6�����cOu>�Fv̔x��9�OEݥjc�ҬfoW�j�З:'µyc�e�*���쭳�d:�^}k�ք�\��\��zu��5C�S����S��'�Q������rCȳ��s��Z��L�n����ww���yV�X}�����u��F�z�j�a^x�t�Y!�Eoy�j|�.�K�[�����<�t}�ZJ��D�e$������U�s-��*�;�]1h�z�������c��0Vީ���b���`������ƛK��\�,4�.�\h��`���[r3�!��)lf>7W{5;{����0��t	�Oe���<�4�
NW�T��g�����v��J�_��_�̗#�A�@��ON	�Ɯ���F3�%JT)���{��L�+H��(	�c�/X�O�`�[��o�"tq��} �)��I��\Wd?�o�u0��fa����ӊ�2v������N��j���<���i���(�fO��JI�h��l��Ÿt��z�+�+��㖣k��~�~��D��}M�G>{��ǥ).:�(_/:~�P)�S����������qc�F,{�F1lg���{��̜�_KY_R"=" ��l� Y6�8�    ����s7�b�r'��<�.��#�cl���T�
Ի`�C���+�d|����#��U۽��V��8t���2m��Fᗨ�'�����	�[�O�F2�b?�D�1-��M��\���-,��3�Wj�U�=94v�����Ĩ���=<>zx��e�iW
�X���V�A7T@m+c<�o�)Zt�e�:��*��%ޑ�luL���t����{��������B)ic={�r�>[	�����=��� �}��UƳ|���HUlq4r�N��D�kO������|L/T��n6�H���;�B�h��zVM���ݴ`��T�f�ɱE�Z��+li[+ֆ�Pe�N6h�����af�pUWOX�m]��}�r�Z%�s�L߄���/M��ڨ/.ԮU�eX_�B3�Q�j�h��s���v���:���f#h1@z�J5�B<*,)�W��7��<��Nrc\7Q�[�+��A�����#= �,�*w�$P&�J��[�ړ\���N�q���{,�o�{�W���:^Taa�h��Hנ��km6�H��SFby��X/9����t	ٳ�� �g9O�X����hk���\���]�7\J���V�����4/ܳh�s Rvnu�����Q7ħ,��+�f*s�h꼟M\8�����lq�kM�m��\�҈���ҥ�k�KK3ת�ܣ���06�M`�N�9��ҶϬ�w?�wS$)�-��p2����*#}��ퟴ�N�s�b�
�۩�It�#s�OD��!������2��N��z?��T�è�wX	�=����JI;=�p��-"�T��i�Ss��kW�K��P��-��=�����:L۱�n�]t�ͥJs�3�9�2[�/�b7���6��;'fr��N)1B<�Q�궱#���*�6��r� �'�ʞd�`+ LN[��;�'�0���E�Z�T��@�#0Q��QrH"X��)? &]c¼Y���p��$6;i����}����3H���%ՅtY$iSҶ��a��X���U
��4f_nѹ	#O��c��y��yn��S��F/B�������wd|��1��â�S��wj�l�~��n��i�%l��Q��Kj�1�A(ң4xDƧKU�,?�wB�i��-
���ئR�8�
T���$P?ĜL#�����W���0����Zk6vG�Ұ��*4��\܇B�$k�8@�;�х5�?�����	ߦ���C[[@)�p��D�Ħ��q��CI��al��ܒ��~Zc���'��Y�'�3$_Ơ(��eA��l\0����9b�!���>�օ[|�8]$�;H���O�� ��m ef�be�Zv�Q5�g8�{�le���[F�a��f6�U�yA������"������FSo[�R�E�m������q~�#�ٳ,"* �1����U�K���L��Kؒ:LŐaf�ͮ��C.��!�6-�.2���M�ka_<�����v):����=�F�3�b�"��o�y�>�������<a/�����H�EDH�&�ͣ"(o�3��F�̶/�jgm����$m��TW�S�A����#�]�̙��%��<�Q툆�P�f�u���*N ���;G8z�4t���cR�O4]!��{��Y��hd�Om-B��nsP�vѲ�����+�Y!��>a�02��|^Hƶ���4q	��A�:+�y�5/��K��8��MU��h��\^����C�T���,�PX���;�"l�'�"��R�)i;�q!�w�!�Z�
�9ʸc�|�(�'�"�쪐&���J��:Z���b��ϑ3u�q���b����z@6��<Mg����a���=(ŝ��3K8��G��t� �p��C�e�mt�"�~�` t��F�z����U�QA�"���;��d�פ1�2)p�.#F4(��;��e�B淃f�n��s5��m�KaJ7�g��S��HV��lh�1T�]��|�W�T�*9�n׎�`+(k��i�_Su6N��7���l�4S7��xO����Q����pf�C����1�C�OL���r��L�'�gc^�1�M_���- ��0��a��Գ�bWv&��]Wס��T�,#;�㺫�'�h��fÙ�^�V��tjn�(�����"�����P�û>"�&[l�N���>���Fua|�C�&ĭ(-�0\���f���m���f樓;M�O(��/ ����W�H����p���=��l7��V·X��q=>���m!(()�n�عy��I]�L����d\
@ѪW�Юm[�$�J�^˘�t�A���X������i�p��>�.�h�B:�2�ć"��lN�.[Gr@�0+	�c�R�Á(�ܶ��4��dl�����Гl>hڧ�n���d���⭎�����f\�C�	Ԓ���T����bΘ�ev2�¢�?/����' U{c�?Iߑ����cSc�S��l���đ�c��k�!��y,�G(/�Uҷ؟@�}����/FȐ7 CY�9�kq9k���&�re��AE�X�S�u=�lc+�n��#U�a;ͱ��B jTM���:������`�P�'پس������.��E��'Җ�v�ރ��;S�f�������d��� �w2����B@�o2���cJq.BZ�n��3���gj	w��S��F�T��t��I�1T
f��K�-*w������={u�2�J�.����	%�mE�k��̫��Jk�r>����T#U	�mۀe������Բ��;�F�J5'ѱ���~TȊ�C_j��5tw��Ƕ��ܗbM.�	o�F�~4���Ml���Ş�X]˿����(j:ݎ�tc����MkD������+�T򁅵3��I!5w�� iO�5�d���/B	�Em�z�Y:��Ƶjsa�2S=�ẃY���5kD��uچ���߱}�:�tf��feh�Q�R��6�Vٝ<l���1_���&ʩ=��´���$;e
$�M:�]�B+v���� �ƗM<1l���&U�D0J�ɏi��le�N�5ݲL"�ĕa�F?D:9��^�*Z��Q�	��>��3�,�X��O5c��d��R��"�ͧ���X|Z�9��=��T;�JJ�v���+�r";W��7��������o�tv��[�����Hv�>��:�Z��g���.7�:�g��~���s��������J�t1Q\.���9�P���{�`j��<;�����zn�]Y�
K���3g�0���L*�.՚����O`�@W�,]��P�>����2���Jm��269$��W� �I���'ڜ�.nu$B��s;x�y����@V�F�@D�Enz�	�a�֓!#zn�1a�*��]��퐁h7<���c 
pW�˒��3ף�JКZz���_�ܱM<�[��4d%)����$��i�-'7�=D5)����-��G�݊�D�|L;ٌ��ƖBka�S��`HJ�x��3K.�u�����^砵�&hf1�ڬ%�Y�CG�-�j߈jQ@�mL�rZ�?U�nٸC7��J4����G�a�w���%	�&BOwQ��C�T_�7�ڈ�^�
�n�0oT�~����2��3̠b4��0�)�S�jE� �,��_�������r����1��oSr������D�P�?���c�ݦA�;lO��Z��ǝ���{���t�Y��U�g�b��^�.�b��/����o��Q���-�LdDiL��T�uS��v�+^D).���CK�)�#b��4�wN?Et��+��Y���"K�m"A��m\r~�2��s2������!`��*�y��A�m'�m�����6�lǞ"�(��<�vP���J��`|뽼�rX��<�u�Q���c
�]M��
��-	tP��"�&���Gd�k]��MN���A�b�m�d�����!e2�b?�+J�* qyG�\��lX�����&�,縉��5�����R�MmtR�6�S����wˀ��<y�I��K���2bX�0:wމSQ�ɨ�N�DF���v���?�)���=    �L�FТ�{�v�j����V�hGjV���ۦh<5��>�D��- �4�-3���1Eٝ�i�cM��b\v~
�#7wɶ�����>�SV�*���#���{k�x��8��-�kD�#��`\���?o'�cgi9�J�sX�E���Y��N��F����m6�.7�׳���w�VՃ	B���p�s�Cs:0ؿ�y�M�0kqQhј���K��jrej��L�����K�y9ߨ��j�����X�uz�bL�˕���^�]����+��7��S��Y����;���m�E·;��$���)F�aY��TI ��*�N���h�ل�	`��8<5~�fƎ���<�g���Qn�����B��Q��ܢ�V�x�u5wYb��9�n�t!Bd�z+Hjf5��O�jn�M��Y�*�x�VDѸ�h~ۉ�r�1t��(�0��y�f�	OS��hK���x�����q��\X���g��Í��E�ˏ�׮�G*�k���J�t>��a V���~�b���}!3'�'�˙@5�[����B��/�����aK�Rȡ��ñ�c棎�F�5�?<q�X�Yi�|譕'{�v RȴK�����p��t���>��3����w�?���9(}kfq15�g�˹���I?Ӿ��\kz���uS����]��q�%�4&3��q%�̠*cI(]�C�Ks#iA/phJ�Sb��Z�;N���vN���ө6�}�Z.�]G(�L�d2���dB�e��$�L��Ƭ.�CL{�LFJa|hB��E�K�L3�Z�bT���(��~5�����*ubA�	\�At>���z�\����l��D=�_	"��eu6K �321<#�*�>zll��đ��V��K���*B�2�{9�_�˙�!&���PZѨJ�B�?�5c� �:.�c�F��/}�r�1u���m�Jdt1��Oq�6� �1��G��I��H�6�<X$�h�%�(}����$�5��h!Ir�)S�xOnih�Wost1$Oٚ���w~�1��2Ĵ2���~a�t/�JͰ�GS !-�v�}�a��N,��h�LUI�X�1��*4��JP<��]��5WӶ���w�8�
G2)�;\�縘]J��ޢ)�oCWv�����9�������dMb��햆Y��d���5{K�OB��~(I���HD����J	{�(����zp�h��)bh)�1
?���=TY��*N�z�j��p���>ی� u'����!��cYq@!�MqP�-B�S(n*ël����;pݦ�V��P#K�j������P�A����9:R�a�����m�T`[���o[^8O��]Y=�v�t�I�$�"�� ��.�H�c�l��H�u�,J.�h�h�l���'Z����v{��p�`�+l�������X���P�8Q�=!Ly���������&X��|�?=g2JU�[����Q'���	��=/oLI[HH��t��2f�&�];SwѠ�6u��
l�=l%L��U��� ^2��IH���1�����\���l�e��6h���鮃N<���J��-f�r����Md���}��Pa���P��P�-{�L���B������,Ю(�oJ�D���-n���W��[ە5�Qe�g�W$11� e�^�"/2�Xz"x)Dנ�#����n7��1�4m���L�,��,���[�����s�Uffz	�\�ʼy��|�[�wJЬi 7�W��w�G��%a5w�ռ���4��D��K�����\�奋9�Z@7�����|F@=�w�A�Om����aMB���SQ��P��@�"s�&!)t�쇍2\E��欇$qH3a�n����C��{���H�G"Q�
��*OX3  ΁#*�r��%��������W����ʅ�e%�c�lnq^�~���9��q��^��GjH^�|��׭֐�
�)�=vJ�&�Ǟ�n;DGS&�hQdecRd�7X&+T��ᵈp�X�{�\���3
\.B���H��E�̊�3��������%v?\�E�mr��u�e�m���.b��V�Hf_!܆��|m~�i�/r�u�g<g�p����_R�b�t�<�jz�<}L�:�����_�<�\�grҪ�L�T�#��3�''������I��b���mt3��2���a�����m��bGA+�YCZ� �.0������vJRC'�L*�U:��d4K� ��-��G�� �l
(K`���ٚ^x�9<\�Vc^�K"òQ��E�ۤ�+��T�h�TY�J��{Z�]��=��e$�[Ž)�r4�o��%<V1A��#�ŵV��9v���+�Ϊ�r��6�<)[�]���@נ|���`>F�jj0��<P�BЇ�ay�������XZ��_��?a��0���e:���'
qZ@eEٖ����x" λ�&8DGC��\�:[��_�s�u�
��_)�����F�y~��&��rp*��Q\�hr�
uYLCWM難�a����D���rԀ�]=BJ�G#Z
B�2��ˍ�V��W�@��]w���6&5&7*�t]8@"fd�`7ک�bo�p{8R�D8&�f��QL������X���zu�%�·U��o��� ��R�Ծ6�;+� �*�驃Ϝ��U�'��~��嵗D�����rC6gf'O�N���bzbbfrRu�������V�PҾ!,�c�e����K�l�)�F���͕eK�pᓚ��b��cX���2nv{qQ��K(��j��SWV3'l��h��qJ$P�(4$��3@%�����E�Iq�^�!�4�X=#��I}9���utA�v��"7�ǥ.@����e$:4�p���n���Ϝ�# Mn�
v��bB�����b���@�1\k�t7���\l��ґ���;J��P�C�ӧ��s��IkWl�}�F��(�.J�s��q6�V֭vw�`���W_��W��E�$Ľ 5��-��r�Jef���b�8T�#����M��K4�|=����$��.���=�VZu8����
��aerC��D)�ZS��mB;ެ��_
t��$��A�Cpb�,�.kFz�ҺBM�n�	UT7��U#���g����pkzZ���s�w�%zG?�e�mf̃!�~F���/Vz�]{x%�,��9�R����YPNG��oqA�ڌ݂�����1��4�U�}�Fk�����8e���ĩ*H���rPЙɉS3S*Py�zA�(:����k͕���w��h���#�%�������y�����ë�LO�w���
n�=��,#:�D���␼�����o�P���iT ����!���=���%�A� �V�sĞ��6��4�p�"Kg��֚[ךﴚ�Vj�6�����X��C��[h�:���X���l�#�c�X����HN�4o#2��hah�`�,,��m�8�?��G�c$�QO!�8��E���UY��^yG��v������� �Z�������9(̳���W�}����Ɗ[�ijZ��v�!U�G��v�@NO��#xB�d0�EBQ�4���`�i��Ts�'d�0��ĚȺ �zq�k`diq�,���T��[s�a����vPٛ��a���{�Ҫ3�UV�*����ɗ�Wy�a��v��XM����7۝��b��^�{���\e
o%F%�N�ƹ�ڎ���;�b͟�nn1�컘�η֬x�m)vx%Ρ)�<�`X|r.�~$�@�����}D��H�2{�/��RCi��G���{Yiی<e���ڨ��!�G�71��N8�~��C`��G����G����8�X��G@�~�$��b8��z��㪗>��,��Z���L>\o^�j���������1'NN�>�S�T�w�x��:�Ev
���92�WQ��٫=8i��l�6U 0�c	���ss�)�)9VFi\<;�X��s���T1,�����7y�5ou��
��\*Y��D�MG6	����@�h?�f�
8ߐ�F�`!�`�p��Lw!l~NY�a��DY�+>ӿ�T��&dV�^��@���M���)M����l�����ᑬ�{"��.3�*    ��Co��q��XQ��%��}/�AOIQKM�ܭ�Z;�*gYj��P3TW!1pt(U(�~-�2��}�*��݁� z������V��9e�=�RFKu�X�%�w�$s��<r�\�<�ҧ_0#��m��!*;�~�ӨDZ#Η�ߺ3����ސua	xIF��� L�]�`d��ԯ��y�zɦ�}B@�� ��T�㮘�����e� R�o�-�������L�\	�NXb��������o����=�V�k�(}�!�� G��Kb�3>Ǩ�`^a�(>mɡP��G��p#�#�t<���0��=#&�-"pB����N/]~�,�en�l��P[z�Q����"���S>7�VP��p)��+��v'�HĻ����pJ��W�!���>/�-r�{��Z�*�I�����B��۞dB-��#�K�K&�?��Y����X˽\��7t
Q�_v`��AV��pF�e�93�"-bXw��d ��1W�R���]���jkj!�/E�ߣ�l9��7��Z�c�����{Φ&m8��c�<��iv��5�y��'���E���o��i��.,��1W��m:�YkO ��=�R@
,/�h_�'b���p_��vi ZR#ޠ�9\c(֖��C�!�Q��ؠ��Rn�E��@&��OW"`�4��;���H�y�Xe3�?��%�oO��H����v�9U�z7:&}%J�T��)& �ˣ&� W��QT+�$�����9�(hNLܽw�b�r�O��l����q4���м@��?暉���'��-9~ΐS����왃�˂N�]�)�Ġӡ�
Y��1�É?ا�eOUo&�z|��̊��Ǐv ��MB���UÃ���'A�~|�aN�f�@H��e���������^�^<��g *J��+T9%�r.����,Ԟ��u=C���c��o�`�۫�Fg��\�=�ΟݲXin]�38�U�-��7��زg�h��x�^�7�Ӝ��s�?G�c��*
=�ɿSSq��6��6�^]m����⬃{b�dw��)�:��~û\=w�AxP �����=��kS���(�>���t�Ӎ�����Eu���n#X�"��ٍ��,d�]��Zߺ־������Zsכ+�ZHrΨ�^Y`K�{�X�V�CW"���/G�9Uy��OK�T?����~�<r�Ao�VW��	<��������M�c����\T8���Xv,2�?����,���� l妝�k�)�,�b>�"�˷�6yE���o'J5GJX)�G�ES����̗���`�GĈ��^�+Zs�j�S����;ؚohմ/���o�o%s� o
�8���-�W����Nk�o�q|,�Va�������a2��xP�J'Q���/1�9����I5j2��a��g�vMj����=�Tp���K�I�Z߼)���S�ܭ�w��a�x�(�����?�9���l�����j6o��[�$ȩٙx�o��;��l��K��s�#��ʯ*^�F;f�>�;�U#��A�s�WWoX�貸��l��l�����nd7cբ�n�	|(f@� zk�V�,*�̄u �<L(J��Z��1�os�OJ�Ȭ�t�9���}����`D��| .��)�)wY��ä>�]�ޛ�]���:��8P<y��`k�͍�������촯�W�ؼ�_�VW���\��س��@V �}fz�Ai�[Ϩf�vp~�B�ڰb�+�A>��93�f��<���g]wG`�b��eΓ�і�lʟ�Y��`?�_�I�8W�8"���M�� �w��*E�X�L�c�/aS!�x�IӁ3����%�L������S3���y����*|�V��b-o����s��m`�9�\1��t�ܣ6n�ɰ�Kv� u��T�:f�&`_��ͧ�:�l�?�o��r��M̡�v�vp|T�������؊`b�c}M6�w76�7���8�]W��?1^�*��1 �3�+s�
vH�3���-/�) ��U�p��I�6`�F.��r��%�'h[ݐf��	�\�_ת;'����	��A�K��"�C�C*ic	'&��THˁ�s�^h��+ҽ��y]�:���r�
��ʸۉGѯ��YX]C�0s��m��c�À�3����8��\
7�>D,D���=Gƃ@V��ȹ���ǈ0��'
x��N�@���굗7�+� ��d��dQAS�>��=��]��-���Lja�\Z�vY̟�P��_�ϟ����&�6L����u%ݣ����K7�_�O]P�I������.���5ݝ\���+�.��f��m�;Π��.P��K�g���u|ǘN�`J���u\���zn ��]�� ˀ�v����!''�C{�A�O��!8~i(W襋�8���J���B5���-
ZkI� 7�͠�D�C"r�*;<�!�>��wCu�ҝ!�K+��fY��Kכ��+D2�B|�q���ZO� ��'�#��)/H(r~�=��t������7:y�o� <�Z��N]�ʇi}���=�S����^�v6�9�B�f�Ww<H�
���G�#`Ħ5��	&���s�/<iQQ��D11��)-,҄������{�� ) ���꜡�!�m'�,��bs@����G�`�w�#�/%�����V����9l7,��cT�IѪ�ʱk&�I_��6�ޱ97�6��c���m��9�!@&�5	�������y+f��D>ܽ��e��2(��G0�>OZ�C���:�\�h��y�es��k�"����ڄdb��oM���d���ӧ�όWy��!ȀQf�kƩƑh�&�ө�[#��Vyl�:��~����vE����3�W��V�|�UG��Ս�71�]mi?��!^-���iePĒ �׎ؠR���v��]�m��D��z��<���㊒Kϒ������=[�ll�HrsbB��Խ�Rgg��\+������� �⡮�	��q@x/�4Ted6XqUm��"�Q@�7�nZ� [L�n^�1�����9�fP�ߡ�M���X���i?��<DQ��Ě�5O�2��ܢ�� ~%(�FQ�XQ��g!�M���^�bx�<&NN`eMcY�\��,�ФF�B�6�.�K2N|��R�S����� R	�
(���� rp��m�Ŧ����������|߾~%��� w3�؅
& 9�#�"�a�R�H�$� F��U��׷N�*�嬋��B�RK���vJ_R�o���In��=f"Iy'�:�}��aw�Y�dh'yK�+:6��R<���+��$��$4��;v��2��PC�C�*�K❟�2�S3��C�a�|4j���jWTQ׌�����:s��������f� �� B��a��~��r���
��	������'d��� 4D�	W���T(�4;�����|0�u�x����D���
r��b"Y�B���C*�0���)��yv�4��:���Fb�-`T�>��~k���_^Zh����)�yu"c,��j�^�7�-���H'���C���Cn&a-�f�����]�7SsvHu.V��RQr�qU��ĵ�VX)�(�Jvˁ)y��HT`?RX�Z��l�@	SZ�w[т���j#MV\���d�&�ap�:�W9�i�lC�D�� 3�%�o�㋇H�{�wL�R*nS�J&��ԅ8���զ�6`�^v��/�f$�`R��n7��9��>�c}���=A��o(r�,��=;�ki.ړ��P���������*�y�yc�:-��Xk�����f�(�����M�IuL-9w�4��f�<��K%�;��������:A����ŕ��N�A��1p���$��sׁ���
��� ����oFW6�m�j	;` �.�FHO�4+־����Ň&�-�JzB=�h��R�4jX�[���Qw���f����@'�m�b{��Ő�_-:�3b�)�}"4��%�?��e@Z?"S���K���=L$T�o�[z�N�����2���&9w�<I�T�4) s  @h�S���*UDv��W@��(H�$%+<9sS�]Q+P��B�"�(���W}��U���������O�!��+Md�a�9���=7��
��N�?���e0T��]\h��H�v�b�¥�9y2��k��������[��������iK��+��*�W�~������˛Mt:�p���Zk���>�������lnm\��6[�NdG6ol�G�9���y&��;u#� �0��g�6WP���p)�m�sa�mgh�v��',�*��F�s��ڰ3U�ۋ�]��|nt��TZI�����X�����3�tFH�	=�A9�c��O�B�=��	����R��(�CG,�m�\𨄗l�H	��  CwX���x�	͝��wJ���x�0���������+��~Y�����N��	ac�"��(��q��q�.�e����6�\mPT����Aۧ<C�B��T�a a0r'e-�/��C���ŉ���]�|�8׼�Wz���:�N����h8�H��a?� v&�V
iq=��KuZ��'��N�ŵ����6G��`]@�JNc�qH��� ���b��!;�]��X�ƻ��lB�k(�3�j��	Z\l�ᲀC��?"�z?���U�lkȌE�.40I@�8���É挓��-��ӠE�9�aer!�g��ӀB�ˠ�H/��A�?)�#n�AҤ<Z�:%7V�����z�����'�[�&�J����	�����tY�Z��@��[�H����݆���C�"B�9M$g3[�����I��1A�a\Rcy0&K	z������mC9�4��?���Ҕ��E"�z�R��s���;r�Kg�F�둆��gAߒD�g㖯\���cՌ4J-�I�]���ߞ�~����������      G   �   x�5�˕!�8�}��2�Ǳ�<s��AMG��T� b��n�v#P��Ԥ.x첺����4X�\�rI����_�o�J�${_��}�}�}�սG:�Q��q�<��l#l��5�+M��D}Ɔg�t��1窋�ٯ=q�|�_�Z!(��ޫ���άBN&�y�#炪o����=R(�Ll�����ݺ�껜�9���1,]��� o$P�%�˃g6�&YSOP�N|GS�Q�)ܞ���,�c�      H   �  x��V�r�F>�<�^ �f4��mM�㵍eR����K�Z�$m��LREPő�`�ٲ��
�7J�$k����֖��t��u�B:���=g�Pr�{ҷ� ���� ~�c�^}�x�2W¼�m��[����)*�c�X�q��8~ k��F�I	��wsv�&B!Џ�D��'}T���z\��z�ǖ>.��i��#=6!���&O��,N���2�ӿ�f�=Mr��x=�kr�Ǫ8����V֏�-�:���>v	�y����Y��0ϩ1$BB7�I�ؑ��Jr�+.���@�2m��|��6��g^�\�&�i����dhj�g��}ߔėP����0sح)F��Q$�ʽ�>�S�ɤ�j�R)g_�E�R���ya�TN4ǀ<Я1�D,��i-��sD=��8�A�(v��8X���~���B��x@(�K
�V:������-D�Y�~�?)I��ϧ)1�C��%d�Ѕ5���kD�B��cds�G�~_њ��9��Z�T�*	V;��e���D���(��j�쨄��yU~�:��+7��)�$�?����!Ş��M�gR'b0.���_�<Ni�8��qy�(�g���p�`�r�Ju哺��g|4#zX�6}1��m�NO-�M�v����MSl�!��Y�P;�s�8��w�!!���zY|�h�ML ��[���r���;����j�Y(k,�H�A5�5���y`j���zTԪy���l��%�د�y�웶��m�����:>®|����2~]�ϰ�G��_���ۥf"4�qF�؀�nAvQ�'1��� ǆ+��%�쟃;�i#����ćNŶZ�#�si��90�7�r��p��j`ͫ<�iH���~N'��(r|]�i���8OS���(ZY��U�-�ÁE~���c�_7}��     