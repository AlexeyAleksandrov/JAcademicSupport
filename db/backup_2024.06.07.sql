PGDMP  3                    |            AcademicSupport    16.2    16.2 l    K           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                      false            L           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                      false            M           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                      false            N           1262    24783    AcademicSupport    DATABASE     �   CREATE DATABASE "AcademicSupport" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Russian_Russia.1251';
 !   DROP DATABASE "AcademicSupport";
                postgres    false            �            1259    24784 
   competency    TABLE     �   CREATE TABLE public.competency (
    id bigint NOT NULL,
    number character varying(5) NOT NULL,
    description text NOT NULL
);
    DROP TABLE public.competency;
       public         heap    postgres    false            �            1259    24789     competency_achievement_indicator    TABLE     o  CREATE TABLE public.competency_achievement_indicator (
    id bigint NOT NULL,
    description character varying(255) NOT NULL,
    indicator_know character varying(255) NOT NULL,
    indicator_able character varying(255) NOT NULL,
    indicator_possess character varying(255) NOT NULL,
    number character varying(10) NOT NULL,
    competency_id bigint NOT NULL
);
 4   DROP TABLE public.competency_achievement_indicator;
       public         heap    postgres    false            �            1259    24794 )   competency_achievement_indicator_keywords    TABLE     �   CREATE TABLE public.competency_achievement_indicator_keywords (
    competency_achievement_indicator_id bigint NOT NULL,
    keywords_id bigint NOT NULL
);
 =   DROP TABLE public.competency_achievement_indicator_keywords;
       public         heap    postgres    false            �            1259    24797    competency_id_seq    SEQUENCE     z   CREATE SEQUENCE public.competency_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 (   DROP SEQUENCE public.competency_id_seq;
       public          postgres    false    215            O           0    0    competency_id_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.competency_id_seq OWNED BY public.competency.id;
          public          postgres    false    218            �            1259    24798    competency_keywords    TABLE     p   CREATE TABLE public.competency_keywords (
    competency_id bigint NOT NULL,
    keywords_id bigint NOT NULL
);
 '   DROP TABLE public.competency_keywords;
       public         heap    postgres    false            �            1259    24801    keyword    TABLE     S   CREATE TABLE public.keyword (
    id bigint NOT NULL,
    keyword text NOT NULL
);
    DROP TABLE public.keyword;
       public         heap    postgres    false            �            1259    24806    keyword_id_seq    SEQUENCE     w   CREATE SEQUENCE public.keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 %   DROP SEQUENCE public.keyword_id_seq;
       public          postgres    false    220            P           0    0    keyword_id_seq    SEQUENCE OWNED BY     A   ALTER SEQUENCE public.keyword_id_seq OWNED BY public.keyword.id;
          public          postgres    false    221            �            1259    25206    keyword_work_skills    TABLE     p   CREATE TABLE public.keyword_work_skills (
    keyword_id bigint NOT NULL,
    work_skills_id bigint NOT NULL
);
 '   DROP TABLE public.keyword_work_skills;
       public         heap    postgres    false            �            1259    25246    recommended_skills    TABLE     �   CREATE TABLE public.recommended_skills (
    id bigint NOT NULL,
    work_skill_id bigint NOT NULL,
    coefficient double precision DEFAULT 0.0 NOT NULL,
    rpd_id bigint
);
 &   DROP TABLE public.recommended_skills;
       public         heap    postgres    false            �            1259    25274    recommended_skills_id_seq    SEQUENCE     �   CREATE SEQUENCE public.recommended_skills_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 0   DROP SEQUENCE public.recommended_skills_id_seq;
       public          postgres    false    236            Q           0    0    recommended_skills_id_seq    SEQUENCE OWNED BY     W   ALTER SEQUENCE public.recommended_skills_id_seq OWNED BY public.recommended_skills.id;
          public          postgres    false    237            �            1259    24807    rpd    TABLE     r   CREATE TABLE public.rpd (
    id bigint NOT NULL,
    discipline_name text NOT NULL,
    year integer NOT NULL
);
    DROP TABLE public.rpd;
       public         heap    postgres    false            �            1259    24812 %   rpd_competency_achievement_indicators    TABLE     �   CREATE TABLE public.rpd_competency_achievement_indicators (
    rpd_id bigint NOT NULL,
    competency_achievement_indicators_id bigint NOT NULL
);
 9   DROP TABLE public.rpd_competency_achievement_indicators;
       public         heap    postgres    false            �            1259    24815 
   rpd_id_seq    SEQUENCE     s   CREATE SEQUENCE public.rpd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 !   DROP SEQUENCE public.rpd_id_seq;
       public          postgres    false    222            R           0    0 
   rpd_id_seq    SEQUENCE OWNED BY     9   ALTER SEQUENCE public.rpd_id_seq OWNED BY public.rpd.id;
          public          postgres    false    224            �            1259    24816 -   rpd_keywords_for_indicator_in_context_rpd_map    TABLE     �   CREATE TABLE public.rpd_keywords_for_indicator_in_context_rpd_map (
    rpd_id bigint NOT NULL,
    keywords_for_indicator_in_context_rpd_map_id bigint NOT NULL,
    keywords_for_indicator_in_context_rpd_map_key bigint NOT NULL
);
 A   DROP TABLE public.rpd_keywords_for_indicator_in_context_rpd_map;
       public         heap    postgres    false            �            1259    25379    rpd_recommended_skills    TABLE     v   CREATE TABLE public.rpd_recommended_skills (
    rpd_id bigint NOT NULL,
    recommended_skills_id bigint NOT NULL
);
 *   DROP TABLE public.rpd_recommended_skills;
       public         heap    postgres    false            �            1259    24819    rpd_recommended_work_skills    TABLE     �   CREATE TABLE public.rpd_recommended_work_skills (
    rpd_id bigint NOT NULL,
    recommended_work_skills_id bigint NOT NULL
);
 /   DROP TABLE public.rpd_recommended_work_skills;
       public         heap    postgres    false            �            1259    24822    skills_group    TABLE     �   CREATE TABLE public.skills_group (
    id bigint NOT NULL,
    description character varying(255),
    market_demand double precision
);
     DROP TABLE public.skills_group;
       public         heap    postgres    false            �            1259    24825    skills_group_id_seq    SEQUENCE     |   CREATE SEQUENCE public.skills_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 *   DROP SEQUENCE public.skills_group_id_seq;
       public          postgres    false    227            S           0    0    skills_group_id_seq    SEQUENCE OWNED BY     K   ALTER SEQUENCE public.skills_group_id_seq OWNED BY public.skills_group.id;
          public          postgres    false    228            �            1259    24826 *   subcompetency_achievement_indicator_id_seq    SEQUENCE     �   CREATE SEQUENCE public.subcompetency_achievement_indicator_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 A   DROP SEQUENCE public.subcompetency_achievement_indicator_id_seq;
       public          postgres    false    216            T           0    0 *   subcompetency_achievement_indicator_id_seq    SEQUENCE OWNED BY     v   ALTER SEQUENCE public.subcompetency_achievement_indicator_id_seq OWNED BY public.competency_achievement_indicator.id;
          public          postgres    false    229            �            1259    24993    vacancy    TABLE     �   CREATE TABLE public.vacancy (
    id bigint NOT NULL,
    name text NOT NULL,
    published_at text,
    hh_id bigint NOT NULL,
    description text
);
    DROP TABLE public.vacancy;
       public         heap    postgres    false            �            1259    24992    vacancy_id_seq    SEQUENCE     w   CREATE SEQUENCE public.vacancy_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 %   DROP SEQUENCE public.vacancy_id_seq;
       public          postgres    false    233            U           0    0    vacancy_id_seq    SEQUENCE OWNED BY     A   ALTER SEQUENCE public.vacancy_id_seq OWNED BY public.vacancy.id;
          public          postgres    false    232            �            1259    25004    vacancy_skills    TABLE     m   CREATE TABLE public.vacancy_skills (
    skills_id bigint NOT NULL,
    vacancy_entity_id bigint NOT NULL
);
 "   DROP TABLE public.vacancy_skills;
       public         heap    postgres    false            �            1259    24827 
   work_skill    TABLE     �   CREATE TABLE public.work_skill (
    id bigint NOT NULL,
    description character varying(255),
    market_demand double precision,
    skills_group_id bigint DEFAULT '-1'::integer NOT NULL
);
    DROP TABLE public.work_skill;
       public         heap    postgres    false            �            1259    24830    work_skill_id_seq    SEQUENCE     z   CREATE SEQUENCE public.work_skill_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 (   DROP SEQUENCE public.work_skill_id_seq;
       public          postgres    false    230            V           0    0    work_skill_id_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.work_skill_id_seq OWNED BY public.work_skill.id;
          public          postgres    false    231            ]           2604    24831    competency id    DEFAULT     n   ALTER TABLE ONLY public.competency ALTER COLUMN id SET DEFAULT nextval('public.competency_id_seq'::regclass);
 <   ALTER TABLE public.competency ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    218    215            ^           2604    24832 #   competency_achievement_indicator id    DEFAULT     �   ALTER TABLE ONLY public.competency_achievement_indicator ALTER COLUMN id SET DEFAULT nextval('public.subcompetency_achievement_indicator_id_seq'::regclass);
 R   ALTER TABLE public.competency_achievement_indicator ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    229    216            _           2604    24833 
   keyword id    DEFAULT     h   ALTER TABLE ONLY public.keyword ALTER COLUMN id SET DEFAULT nextval('public.keyword_id_seq'::regclass);
 9   ALTER TABLE public.keyword ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    221    220            e           2604    25275    recommended_skills id    DEFAULT     ~   ALTER TABLE ONLY public.recommended_skills ALTER COLUMN id SET DEFAULT nextval('public.recommended_skills_id_seq'::regclass);
 D   ALTER TABLE public.recommended_skills ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    237    236            `           2604    24834    rpd id    DEFAULT     `   ALTER TABLE ONLY public.rpd ALTER COLUMN id SET DEFAULT nextval('public.rpd_id_seq'::regclass);
 5   ALTER TABLE public.rpd ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    224    222            a           2604    24835    skills_group id    DEFAULT     r   ALTER TABLE ONLY public.skills_group ALTER COLUMN id SET DEFAULT nextval('public.skills_group_id_seq'::regclass);
 >   ALTER TABLE public.skills_group ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    228    227            d           2604    24996 
   vacancy id    DEFAULT     h   ALTER TABLE ONLY public.vacancy ALTER COLUMN id SET DEFAULT nextval('public.vacancy_id_seq'::regclass);
 9   ALTER TABLE public.vacancy ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    233    232    233            b           2604    24836    work_skill id    DEFAULT     n   ALTER TABLE ONLY public.work_skill ALTER COLUMN id SET DEFAULT nextval('public.work_skill_id_seq'::regclass);
 <   ALTER TABLE public.work_skill ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    231    230            1          0    24784 
   competency 
   TABLE DATA           =   COPY public.competency (id, number, description) FROM stdin;
    public          postgres    false    215   �       2          0    24789     competency_achievement_indicator 
   TABLE DATA           �   COPY public.competency_achievement_indicator (id, description, indicator_know, indicator_able, indicator_possess, number, competency_id) FROM stdin;
    public          postgres    false    216   L�       3          0    24794 )   competency_achievement_indicator_keywords 
   TABLE DATA           u   COPY public.competency_achievement_indicator_keywords (competency_achievement_indicator_id, keywords_id) FROM stdin;
    public          postgres    false    217   ^�       5          0    24798    competency_keywords 
   TABLE DATA           I   COPY public.competency_keywords (competency_id, keywords_id) FROM stdin;
    public          postgres    false    219   ��       6          0    24801    keyword 
   TABLE DATA           .   COPY public.keyword (id, keyword) FROM stdin;
    public          postgres    false    220   ߒ       E          0    25206    keyword_work_skills 
   TABLE DATA           I   COPY public.keyword_work_skills (keyword_id, work_skills_id) FROM stdin;
    public          postgres    false    235   P�       F          0    25246    recommended_skills 
   TABLE DATA           T   COPY public.recommended_skills (id, work_skill_id, coefficient, rpd_id) FROM stdin;
    public          postgres    false    236   ֔       8          0    24807    rpd 
   TABLE DATA           8   COPY public.rpd (id, discipline_name, year) FROM stdin;
    public          postgres    false    222   ӕ       9          0    24812 %   rpd_competency_achievement_indicators 
   TABLE DATA           m   COPY public.rpd_competency_achievement_indicators (rpd_id, competency_achievement_indicators_id) FROM stdin;
    public          postgres    false    223   B�       ;          0    24816 -   rpd_keywords_for_indicator_in_context_rpd_map 
   TABLE DATA           �   COPY public.rpd_keywords_for_indicator_in_context_rpd_map (rpd_id, keywords_for_indicator_in_context_rpd_map_id, keywords_for_indicator_in_context_rpd_map_key) FROM stdin;
    public          postgres    false    225   s�       H          0    25379    rpd_recommended_skills 
   TABLE DATA           O   COPY public.rpd_recommended_skills (rpd_id, recommended_skills_id) FROM stdin;
    public          postgres    false    238   ��       <          0    24819    rpd_recommended_work_skills 
   TABLE DATA           Y   COPY public.rpd_recommended_work_skills (rpd_id, recommended_work_skills_id) FROM stdin;
    public          postgres    false    226   ��       =          0    24822    skills_group 
   TABLE DATA           F   COPY public.skills_group (id, description, market_demand) FROM stdin;
    public          postgres    false    227   ��       C          0    24993    vacancy 
   TABLE DATA           M   COPY public.vacancy (id, name, published_at, hh_id, description) FROM stdin;
    public          postgres    false    233   �       D          0    25004    vacancy_skills 
   TABLE DATA           F   COPY public.vacancy_skills (skills_id, vacancy_entity_id) FROM stdin;
    public          postgres    false    234   ��       @          0    24827 
   work_skill 
   TABLE DATA           U   COPY public.work_skill (id, description, market_demand, skills_group_id) FROM stdin;
    public          postgres    false    230   ��       W           0    0    competency_id_seq    SEQUENCE SET     @   SELECT pg_catalog.setval('public.competency_id_seq', 14, true);
          public          postgres    false    218            X           0    0    keyword_id_seq    SEQUENCE SET     =   SELECT pg_catalog.setval('public.keyword_id_seq', 57, true);
          public          postgres    false    221            Y           0    0    recommended_skills_id_seq    SEQUENCE SET     J   SELECT pg_catalog.setval('public.recommended_skills_id_seq', 1942, true);
          public          postgres    false    237            Z           0    0 
   rpd_id_seq    SEQUENCE SET     8   SELECT pg_catalog.setval('public.rpd_id_seq', 3, true);
          public          postgres    false    224            [           0    0    skills_group_id_seq    SEQUENCE SET     B   SELECT pg_catalog.setval('public.skills_group_id_seq', 15, true);
          public          postgres    false    228            \           0    0 *   subcompetency_achievement_indicator_id_seq    SEQUENCE SET     X   SELECT pg_catalog.setval('public.subcompetency_achievement_indicator_id_seq', 5, true);
          public          postgres    false    229            ]           0    0    vacancy_id_seq    SEQUENCE SET     =   SELECT pg_catalog.setval('public.vacancy_id_seq', 21, true);
          public          postgres    false    232            ^           0    0    work_skill_id_seq    SEQUENCE SET     A   SELECT pg_catalog.setval('public.work_skill_id_seq', 108, true);
          public          postgres    false    231            n           2606    24838 L   competency_achievement_indicator competency_achievement_indicator_number_key 
   CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT competency_achievement_indicator_number_key UNIQUE (number);
 v   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT competency_achievement_indicator_number_key;
       public            postgres    false    216            p           2606    24840 F   competency_achievement_indicator competency_achievement_indicator_pkey 
   CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT competency_achievement_indicator_pkey PRIMARY KEY (id);
 p   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT competency_achievement_indicator_pkey;
       public            postgres    false    216            h           2606    24842    competency competency_id_key 
   CONSTRAINT     U   ALTER TABLE ONLY public.competency
    ADD CONSTRAINT competency_id_key UNIQUE (id);
 F   ALTER TABLE ONLY public.competency DROP CONSTRAINT competency_id_key;
       public            postgres    false    215            j           2606    24844     competency competency_number_key 
   CONSTRAINT     ]   ALTER TABLE ONLY public.competency
    ADD CONSTRAINT competency_number_key UNIQUE (number);
 J   ALTER TABLE ONLY public.competency DROP CONSTRAINT competency_number_key;
       public            postgres    false    215            l           2606    24846    competency competency_pkey 
   CONSTRAINT     X   ALTER TABLE ONLY public.competency
    ADD CONSTRAINT competency_pkey PRIMARY KEY (id);
 D   ALTER TABLE ONLY public.competency DROP CONSTRAINT competency_pkey;
       public            postgres    false    215            t           2606    25434    keyword keyword_keyword_key 
   CONSTRAINT     Y   ALTER TABLE ONLY public.keyword
    ADD CONSTRAINT keyword_keyword_key UNIQUE (keyword);
 E   ALTER TABLE ONLY public.keyword DROP CONSTRAINT keyword_keyword_key;
       public            postgres    false    220            v           2606    24850    keyword keyword_pkey 
   CONSTRAINT     R   ALTER TABLE ONLY public.keyword
    ADD CONSTRAINT keyword_pkey PRIMARY KEY (id);
 >   ALTER TABLE ONLY public.keyword DROP CONSTRAINT keyword_pkey;
       public            postgres    false    220            �           2606    25277 ,   recommended_skills recommended_skills_id_key 
   CONSTRAINT     e   ALTER TABLE ONLY public.recommended_skills
    ADD CONSTRAINT recommended_skills_id_key UNIQUE (id);
 V   ALTER TABLE ONLY public.recommended_skills DROP CONSTRAINT recommended_skills_id_key;
       public            postgres    false    236            �           2606    25251 (   recommended_skills recommended_skills_pk 
   CONSTRAINT     f   ALTER TABLE ONLY public.recommended_skills
    ADD CONSTRAINT recommended_skills_pk PRIMARY KEY (id);
 R   ALTER TABLE ONLY public.recommended_skills DROP CONSTRAINT recommended_skills_pk;
       public            postgres    false    236            z           2606    24852 `   rpd_keywords_for_indicator_in_context_rpd_map rpd_keywords_for_indicator_in_context_rpd_map_pkey 
   CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT rpd_keywords_for_indicator_in_context_rpd_map_pkey PRIMARY KEY (rpd_id, keywords_for_indicator_in_context_rpd_map_key);
 �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT rpd_keywords_for_indicator_in_context_rpd_map_pkey;
       public            postgres    false    225    225            x           2606    24854    rpd rpd_pkey 
   CONSTRAINT     J   ALTER TABLE ONLY public.rpd
    ADD CONSTRAINT rpd_pkey PRIMARY KEY (id);
 6   ALTER TABLE ONLY public.rpd DROP CONSTRAINT rpd_pkey;
       public            postgres    false    222            ~           2606    24856     skills_group skills_group_id_key 
   CONSTRAINT     Y   ALTER TABLE ONLY public.skills_group
    ADD CONSTRAINT skills_group_id_key UNIQUE (id);
 J   ALTER TABLE ONLY public.skills_group DROP CONSTRAINT skills_group_id_key;
       public            postgres    false    227            �           2606    24858    skills_group skills_group_pk 
   CONSTRAINT     Z   ALTER TABLE ONLY public.skills_group
    ADD CONSTRAINT skills_group_pk PRIMARY KEY (id);
 F   ALTER TABLE ONLY public.skills_group DROP CONSTRAINT skills_group_pk;
       public            postgres    false    227            r           2606    24860 K   competency_achievement_indicator subcompetency_achievement_indicator_id_key 
   CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT subcompetency_achievement_indicator_id_key UNIQUE (id);
 u   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT subcompetency_achievement_indicator_id_key;
       public            postgres    false    216            |           2606    24862 7   rpd_recommended_work_skills uk425s9mbq3ruhfaib3ln6s19cr 
   CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_work_skills
    ADD CONSTRAINT uk425s9mbq3ruhfaib3ln6s19cr UNIQUE (recommended_work_skills_id);
 a   ALTER TABLE ONLY public.rpd_recommended_work_skills DROP CONSTRAINT uk425s9mbq3ruhfaib3ln6s19cr;
       public            postgres    false    226            �           2606    25027    vacancy vacancy_hh_id_key 
   CONSTRAINT     U   ALTER TABLE ONLY public.vacancy
    ADD CONSTRAINT vacancy_hh_id_key UNIQUE (hh_id);
 C   ALTER TABLE ONLY public.vacancy DROP CONSTRAINT vacancy_hh_id_key;
       public            postgres    false    233            �           2606    25000    vacancy vacancy_pkey 
   CONSTRAINT     R   ALTER TABLE ONLY public.vacancy
    ADD CONSTRAINT vacancy_pkey PRIMARY KEY (id);
 >   ALTER TABLE ONLY public.vacancy DROP CONSTRAINT vacancy_pkey;
       public            postgres    false    233            �           2606    24872    work_skill work_skill_id_key 
   CONSTRAINT     U   ALTER TABLE ONLY public.work_skill
    ADD CONSTRAINT work_skill_id_key UNIQUE (id);
 F   ALTER TABLE ONLY public.work_skill DROP CONSTRAINT work_skill_id_key;
       public            postgres    false    230            �           2606    24874    work_skill work_skill_pkey 
   CONSTRAINT     X   ALTER TABLE ONLY public.work_skill
    ADD CONSTRAINT work_skill_pkey PRIMARY KEY (id);
 D   ALTER TABLE ONLY public.work_skill DROP CONSTRAINT work_skill_pkey;
       public            postgres    false    230            �           2606    24875 R   competency_achievement_indicator competency_achievement_indicator_competency_id_fk    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT competency_achievement_indicator_competency_id_fk FOREIGN KEY (competency_id) REFERENCES public.competency(id);
 |   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT competency_achievement_indicator_competency_id_fk;
       public          postgres    false    216    4712    215            �           2606    24880 I   rpd_keywords_for_indicator_in_context_rpd_map fk1sb6pdj6rjp9elxtc5ef0tige    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT fk1sb6pdj6rjp9elxtc5ef0tige FOREIGN KEY (keywords_for_indicator_in_context_rpd_map_id) REFERENCES public.keyword(id);
 s   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT fk1sb6pdj6rjp9elxtc5ef0tige;
       public          postgres    false    220    4726    225            �           2606    24885 H   rpd_keywords_for_indicator_in_context_rpd_map fk2quehu5yoisgnidf2kikdnlp    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT fk2quehu5yoisgnidf2kikdnlp FOREIGN KEY (rpd_id) REFERENCES public.rpd(id);
 r   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT fk2quehu5yoisgnidf2kikdnlp;
       public          postgres    false    225    4728    222            �           2606    25411 *   vacancy_skills fk3lxhcg6nbdf1gq6gau2qwrgfp    FK CONSTRAINT     �   ALTER TABLE ONLY public.vacancy_skills
    ADD CONSTRAINT fk3lxhcg6nbdf1gq6gau2qwrgfp FOREIGN KEY (skills_id) REFERENCES public.work_skill(id) ON UPDATE CASCADE ON DELETE CASCADE;
 T   ALTER TABLE ONLY public.vacancy_skills DROP CONSTRAINT fk3lxhcg6nbdf1gq6gau2qwrgfp;
       public          postgres    false    234    230    4740            �           2606    25416 /   keyword_work_skills fk4k5q1ts47obcutvixud6r5jol    FK CONSTRAINT     �   ALTER TABLE ONLY public.keyword_work_skills
    ADD CONSTRAINT fk4k5q1ts47obcutvixud6r5jol FOREIGN KEY (work_skills_id) REFERENCES public.work_skill(id) ON UPDATE CASCADE ON DELETE CASCADE;
 Y   ALTER TABLE ONLY public.keyword_work_skills DROP CONSTRAINT fk4k5q1ts47obcutvixud6r5jol;
       public          postgres    false    235    4740    230            �           2606    24890 E   competency_achievement_indicator_keywords fk6ihuse5jevucy5liuoh2w6ovs    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator_keywords
    ADD CONSTRAINT fk6ihuse5jevucy5liuoh2w6ovs FOREIGN KEY (competency_achievement_indicator_id) REFERENCES public.competency_achievement_indicator(id);
 o   ALTER TABLE ONLY public.competency_achievement_indicator_keywords DROP CONSTRAINT fk6ihuse5jevucy5liuoh2w6ovs;
       public          postgres    false    4720    217    216            �           2606    24895 7   rpd_recommended_work_skills fk9pygp1m1u9yxesq9a8wtl86nh    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_work_skills
    ADD CONSTRAINT fk9pygp1m1u9yxesq9a8wtl86nh FOREIGN KEY (rpd_id) REFERENCES public.rpd(id);
 a   ALTER TABLE ONLY public.rpd_recommended_work_skills DROP CONSTRAINT fk9pygp1m1u9yxesq9a8wtl86nh;
       public          postgres    false    222    4728    226            �           2606    25394 .   recommended_skills fk9wms8xsw6d3nenoa5xe2ma16w    FK CONSTRAINT     �   ALTER TABLE ONLY public.recommended_skills
    ADD CONSTRAINT fk9wms8xsw6d3nenoa5xe2ma16w FOREIGN KEY (rpd_id) REFERENCES public.rpd(id);
 X   ALTER TABLE ONLY public.recommended_skills DROP CONSTRAINT fk9wms8xsw6d3nenoa5xe2ma16w;
       public          postgres    false    236    222    4728            �           2606    24900 A   rpd_competency_achievement_indicators fk9yincl0yk3dec9dqfr76c9w9s    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_competency_achievement_indicators
    ADD CONSTRAINT fk9yincl0yk3dec9dqfr76c9w9s FOREIGN KEY (rpd_id) REFERENCES public.rpd(id);
 k   ALTER TABLE ONLY public.rpd_competency_achievement_indicators DROP CONSTRAINT fk9yincl0yk3dec9dqfr76c9w9s;
       public          postgres    false    222    223    4728            �           2606    25426 7   rpd_recommended_work_skills fkdl0wgopbuj6j7l387wyiw0pe3    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_work_skills
    ADD CONSTRAINT fkdl0wgopbuj6j7l387wyiw0pe3 FOREIGN KEY (recommended_work_skills_id) REFERENCES public.work_skill(id) ON UPDATE CASCADE ON DELETE CASCADE;
 a   ALTER TABLE ONLY public.rpd_recommended_work_skills DROP CONSTRAINT fkdl0wgopbuj6j7l387wyiw0pe3;
       public          postgres    false    4740    226    230            �           2606    24910 A   rpd_competency_achievement_indicators fkdoed4n0dilyh3bb0dp2hjk20f    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_competency_achievement_indicators
    ADD CONSTRAINT fkdoed4n0dilyh3bb0dp2hjk20f FOREIGN KEY (competency_achievement_indicators_id) REFERENCES public.competency_achievement_indicator(id);
 k   ALTER TABLE ONLY public.rpd_competency_achievement_indicators DROP CONSTRAINT fkdoed4n0dilyh3bb0dp2hjk20f;
       public          postgres    false    216    4720    223            �           2606    25214 /   keyword_work_skills fkfc8xqj21xypdk76tgtrkfexmr    FK CONSTRAINT     �   ALTER TABLE ONLY public.keyword_work_skills
    ADD CONSTRAINT fkfc8xqj21xypdk76tgtrkfexmr FOREIGN KEY (keyword_id) REFERENCES public.keyword(id);
 Y   ALTER TABLE ONLY public.keyword_work_skills DROP CONSTRAINT fkfc8xqj21xypdk76tgtrkfexmr;
       public          postgres    false    220    235    4726            �           2606    24915 I   rpd_keywords_for_indicator_in_context_rpd_map fkgfpvx2jsey6jlhoqoogsjg25l    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT fkgfpvx2jsey6jlhoqoogsjg25l FOREIGN KEY (keywords_for_indicator_in_context_rpd_map_key) REFERENCES public.competency_achievement_indicator(id);
 s   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT fkgfpvx2jsey6jlhoqoogsjg25l;
       public          postgres    false    4720    216    225            �           2606    25187 E   competency_achievement_indicator_keywords fkhux4o70yjb812s53ka1bcwwav    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator_keywords
    ADD CONSTRAINT fkhux4o70yjb812s53ka1bcwwav FOREIGN KEY (keywords_id) REFERENCES public.keyword(id) ON UPDATE CASCADE ON DELETE CASCADE;
 o   ALTER TABLE ONLY public.competency_achievement_indicator_keywords DROP CONSTRAINT fkhux4o70yjb812s53ka1bcwwav;
       public          postgres    false    220    217    4726            �           2606    25387 2   rpd_recommended_skills fkj7wqecaxj4bxe1kbftr48q2ig    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_skills
    ADD CONSTRAINT fkj7wqecaxj4bxe1kbftr48q2ig FOREIGN KEY (rpd_id) REFERENCES public.rpd(id);
 \   ALTER TABLE ONLY public.rpd_recommended_skills DROP CONSTRAINT fkj7wqecaxj4bxe1kbftr48q2ig;
       public          postgres    false    238    4728    222            �           2606    25019 *   vacancy_skills fkja9glj7ao8pyy3k59p56iuyt3    FK CONSTRAINT     �   ALTER TABLE ONLY public.vacancy_skills
    ADD CONSTRAINT fkja9glj7ao8pyy3k59p56iuyt3 FOREIGN KEY (vacancy_entity_id) REFERENCES public.vacancy(id);
 T   ALTER TABLE ONLY public.vacancy_skills DROP CONSTRAINT fkja9glj7ao8pyy3k59p56iuyt3;
       public          postgres    false    234    4744    233            �           2606    25192 /   competency_keywords fkopdasmcmo9iw4gges2ignt0tx    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_keywords
    ADD CONSTRAINT fkopdasmcmo9iw4gges2ignt0tx FOREIGN KEY (keywords_id) REFERENCES public.keyword(id) ON UPDATE CASCADE ON DELETE CASCADE;
 Y   ALTER TABLE ONLY public.competency_keywords DROP CONSTRAINT fkopdasmcmo9iw4gges2ignt0tx;
       public          postgres    false    4726    220    219            �           2606    24930 /   competency_keywords fkptcv7s0w2swcpk6han7ta3q9s    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_keywords
    ADD CONSTRAINT fkptcv7s0w2swcpk6han7ta3q9s FOREIGN KEY (competency_id) REFERENCES public.competency(id);
 Y   ALTER TABLE ONLY public.competency_keywords DROP CONSTRAINT fkptcv7s0w2swcpk6han7ta3q9s;
       public          postgres    false    4712    215    219            �           2606    25382 2   rpd_recommended_skills fkquv63vg9xvwng9i5w6rrai9mw    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_skills
    ADD CONSTRAINT fkquv63vg9xvwng9i5w6rrai9mw FOREIGN KEY (recommended_skills_id) REFERENCES public.recommended_skills(id);
 \   ALTER TABLE ONLY public.rpd_recommended_skills DROP CONSTRAINT fkquv63vg9xvwng9i5w6rrai9mw;
       public          postgres    false    238    4748    236            �           2606    25421 6   recommended_skills recommended_skills_work_skill_id_fk    FK CONSTRAINT     �   ALTER TABLE ONLY public.recommended_skills
    ADD CONSTRAINT recommended_skills_work_skill_id_fk FOREIGN KEY (work_skill_id) REFERENCES public.work_skill(id) ON UPDATE CASCADE ON DELETE CASCADE;
 `   ALTER TABLE ONLY public.recommended_skills DROP CONSTRAINT recommended_skills_work_skill_id_fk;
       public          postgres    false    236    4740    230            �           2606    24935 (   work_skill work_skill_skills_group_id_fk    FK CONSTRAINT     �   ALTER TABLE ONLY public.work_skill
    ADD CONSTRAINT work_skill_skills_group_id_fk FOREIGN KEY (skills_group_id) REFERENCES public.skills_group(id);
 R   ALTER TABLE ONLY public.work_skill DROP CONSTRAINT work_skill_skills_group_id_fk;
       public          postgres    false    4734    230    227            1   4  x��TQR�@�Ƨ�$�J�ѫ�0qRL��:��q��;�+ho�{��	���$�+�IOz��L��<�/��Y�b!A�R���(;��J��q��.��w��VY�F �᷈�N~�g�8�1N���8i�]���˄�&h����_��T����AZ��Q@#�c9@��nV����eCHHR�4+!H���p4qO"ʞ��x���1T��߼�W&.:���1=��az`u�R���Y��9\��C��Jш��9���I�e����B��&�)����"��Y��>��޺/[�����J�R��L(�K��D���A
�����ZB6�eA&�#z)PU���+��'�9
dSq�_�_�9^�!m|�J��w��q���%p�����{U��ԭ�lN�AV���6���YpT$m�_&b�Zʦ{Hgq�E�+������;.FI+neu�f�~[Vz$�~��Ȉ�����pL�'ߠ#���e������`9�Y��_���E�x�}��1m�'].�UP~�r0qp%mT������1uN�J]�M�����:ϲ�Q6�      2     x��VQn�@�Ƨ��H@�^����T��ԯ�M�`L,���
37꛷k%k��DQ�xgw���7o�ߓ'I!ɥ�G�5�u,�d��03�c̪�Fj6��7|׻�&Xz��Z��] X��,k}�-��1S�OYJ�H��X���H�����)�3��2��hɛALL�¾Cz��,�Ls���ކ����O6'Ԕ�1}k����D�A[�-h[��v"����&��+�l+G�ձ�_c;�R'~Ypx���(k+vn)d���$PĈ �)-3����B�&y����~�z�{V�R6z�-�Rs��1�]9u��fmd�I�ä�}jE�rL�s۝Q���Z�����^)��'|@y���� ��^d�`*�M*CZGDyf�
�����k�����GC#�b���eW����Ȟ5��q�i�^{���&$@�:�Һ��;�M,^��s�Ƌ!%�/}��G�L�`�"X�]�|<���ZcVr�Ex���9���y�@��N-o�7 �Qֵ� �2�m'Y�Y� gz[���5ωW���ctˑ�����\�d�OY��p�V,��KkN9�B#�Ř�D�l�{I�{^��h��b�����zc����W��u�&������{]�g�����i��^s��j,ِi#��f8�����.x�6{%��t�W�#���F'*N�'��5K��+��c(��ܞM~��~a����Y����rOߒ��KoK]U���v$�kt���^�K#h��z������eE� �v@�      3   3   x�ȱ  �99�6��/�e�`AIAŀ�#&�����3�f�����Fi      5   .   x�3�4�24�44�24�42�`Ғ��J� ՘p�q��qqq ��6      6   a  x�}R�m�0�mO���#j�K���m*%#t�a� ��6�wM�(��m/74��୰:P������K,"58�46MN/���z�������kG{�48��MZyg�ܠ!�D�h��	e������=u�S=g������=TEѦJ/X���=��������n:�G@�Ħ+3Պ�{5g�����
�㙣�#T�Sv��-�F����_�z�#�')��@=om^L�e�k4 _�K���w/p_��X�kG��5V[�55�h%���b��A�\���4%�^���2���h��Hm� ��۔f�2�9��̽��NJ��v�p�0�ݺ�5��W�-&�5��� �!�      E   v   x�%��D1�s(f������E� E��$kM]�W3h��]C�C\���<qUH
;�(��������X��kܵ�4O0T���dd�3&�cE��bm����Z]�R�k#i��GD�J'�      F   �   x�u�K��0D��ä �����V���k���#N"��^�h;�qpC��2��ƻp9���|4�h`��v���	�[2a�&sٍ�$�60ـ��F��l��TBڭF��9 ����]k���Ai �JBW�WSBeJ;`�����*���ot �L���i��Gx�3��n�H�� X{e0�����RM��p5����g��O��ZйC���z�x4.�~���<�S      8   _   x�3估��֋��^�wa7o���� ����/6]l �6\�}��h�}
��L ��� ��o�����FF&\�44;F��� �|�      9   !   x�3�4�2�4�2��`��M�ؔ+F��� C��      ;      x������ � �      H      x������ � �      <   7   x�ʱ  ��/�D�^�y�M�TܡQ8�Z��a/r��"#��N� >��
X      =     x��PKJA]W�bN�g?���A�!7��I@A���UT<@2f���
�o�란�����_�O������a��@�F�i"xs]�b��,�LS�m_�}����Msw�naQ�D�("1§D���T3���>��"
������RD��u]��S�u���D�?�t]����#Z���w���)͝���L,x���I���!�,D
�OmW�	ii�;�����gRٛ�K���������+|`W���/����]�\�+a?�I��ֳfz�P�o��/      C      x����r\ו&��z���� x�Db� *\�4EI�(�};�'	$ɴ@$:��D�� �rQ�$�b�(R�]7T�� RLL�=��W��+ܽ�=�L��[�]vr8g�=��[�*ɞtV�F�C��,t�կ��糬�Y���Z�Α��с���������bq|�xthdh���'��'�Nym�'�-�k��+?ɾ��{�Y+�vԯ˝��ڝ�$�H������z}?kg{I��>����|��_�K����Iv����u�>{���>�����d�Y;�fO��$�x�}���s7k°�fryinn�^�y?xb5�V�w�Gߜ�W��o��-~�㦺�z���[u���N�����+0P�k��Y���g�:�ҹ�P?����|���i���!�+�j��V�5��;8�z�~������'�8S0�'�&Ag)Q�	Wڅ��K�4{�d_d�����~��*;��O����TC���Uoo�����D��RϺ�����a;�|O�V��޷6�M\��zֱ�mf��K��Yc0P��S[�"c�����/.ͩs���n��ہ/�:��7h3��6a�6�m5}7q��p�n§i�o�b�^+���E����6�^�����-t�S��a�j=�����<�}��0����	�62�E`3y�T�M�ޯ���7�&0d5��f\���ḩ㢶YC=[�����6m�-ؗ	�^P=tv�ڝ�u�sGv�&�'u���8�r5{c�=�:�N�-���v�ŀ�]aUP0l�r��T�lÓ����tn�M�W0D����@x8wI tn�����xkº/����zy��Q��S2L�;��g��l��t���g,)����9����ψ��͞�ѓ�:��R���YB7Ʃ���灭�� �¹c���v@H' Oc�V��j[}j�o���J��y����J:����߇����Z���-R*�i�:����	�Ǹ⤑��@��Dl��\�)�r{K��'<�mb{���',�Y�m��_Uw����I�D�V��H�1<$ wX�6QE���%'@�S���+Xn>��O�'�`l=�E*3G:Y;�3O�3�%`��YKi��H�����t#IǢ*����L�E8�j䟲'����~/>��D+���s��l��a�d�A�*��xL��jTJ��$Q��#���>�'8���Zd�9RE��g(8q��9���Id1�r��l#���U�`Cm��t.W�*���U3���?n�=��4N���o�╽��,8�z�/�X���ƿ����_$�d ����Z��ժM��c��^�N�e��2@��2�]qc����]�ɯ��K�j-�[�hMyF�Se5<��*]z/�����������yK�$�7�7-Z
{z�7hF�i�~��I��G��/EZ�I[�f�D?�����o���צ[��>h΁A������BX�5�6�w�MWЄP����uڜ���v)H1�;
��	�}Zc��7���5/�~��q�3��R;/uL����D�壨i/˅�����4y��Aij�VY���Pg_�([���e�����S~��={B�;�71z��$p�=\;�
K&�˵��a�ܺp����ۣ�+3G�C_�v]���������%�'c~���ӻ�Za5ϩ�P9UH�r��7��/t�u��]���>�\�n��WF����'}ڳ�7^�������c���ƆǊ��#������e�=���Bm�vb�W�-P���%�Hۛ�������_��GZ¤�$�l(%�q���L�L��K�����Ή	��"R��СR�sY+�t�&ل���
""@��[p�l�􁃴g�	�۠p��Q��Y}~z ��SO���՛p~S��:��3��dC"P��a��{()YR(fUd��|��� �)j^��z����Q�{�����F��3�0B�+uC{����Dvf��������HI�zi�m�l�>ǫ���AӼKt+{�O��X,�@ueL:0�:wA����>O=+��-2�>��b���Q�[�s�����'u�Jk�F}���[���)7Y@m�݁ �-4}�#%�"+�'E�p%wDxo%$%Y"n������r�ct,@��j��n|���Q#x�ָ��}�iB��+��Q�5�x���o.VO�Z�S�u�f�~Ŭ�3��!����� Ͻ��A��Yc�IF�
i\�g�ۖ ���d��9FTl��X�����㠦���[�p��&���Hl�.���f����p��-XШ+��7C�=7�T�]����e�)�P���2M����Q�~�u�{�a��Hbh��cߦ��S�\�~Q��k��_A�6Й�g��f����� �3ώw"Ȟ�]�Ө�V[w�g<gɌF�SM�h�#􆖆�>�����byd�B5�&�&�j�p�Q0�j���y]����q43�dj�V�����W��~V?Uߪ돒��&����������R-MNOM��<�1=��<�+�G�K99���#c�G��������'$Yq�����1��Y���%2o��Zi�:WY$+Fgg$ʶy����ty檕"H�ڲ�g�f��DGn�(O񋶴��-2/����hS��R��-:K�߲���� OT[�ꖌE��Sr}�9i�F�-J��69�#8`{���VQ�5��)�m7��Y1�E9[�i&�2Qr!4���w"�0�x>I����{��*�aĉ^���7Ju=P6���A��<S�Oߐ�買Q���%�66��R�i�t��0���0�[�Yu k�.�8n��O�h~ҮI�pr�`sJƱ�i+���Dէf��TզH&��=�:�u�ٚ ��wԲ^�l��Wa�����3
ް��fM"	��j��g�<U�K�J����B�^�K&K�+�k��z��0����"�h�Q�=��7ԅ���A�=��Q����-T8�|�[���->��L�_�j���A�����s�O:r0-[�����}N�4vn���@����}�f��,?��1�d�W8�oܴ?��#�VQ���±uO���M��h	��I�A�O��z�l��q��(��;�ֈhsq�6"b�'��-0���W�d�qSgn�:耬+͹����)G1NՀ�D��T]_��S��o�y�ϋX� �ɉ�0��y�a�~C��2��۶�i��̛�cd9��]q�����cCCA^2ub�9�l�
��ޏk�ӓ�ǀg��q8R����3�j�}	;�(�N+E���2�*6�܊�m@2$"���
��a��,�DN��X F
Rco@[:�{�w���~1)�/��q�m�sP*�X{��kƵG�)>"���h^a� O(�t���M+���� lY�ݳa����g���� ����S�� n�}Ɂ�G�ߧٿ���;�;��y��B���~x=h���e+� F� ݐ����}��Κ�JW��;9	����bZ���������E����V�-I9�j�Z���������)/�J�����g�P�qNӄ��l8� D"�G����m �-�Q�!E!r��p�!f��ɦ\�]����E��Qw ,|U�Q<G �v|5e����f@<�����z����Ň ן�#/��:'�fU{���7�!���K�w.N���K��|�^N�+�UfK�*x��k�yeg%�J�����瓟�J��Vk�ɛ?�������ee�]+ϩ�f�ʓ��٫��%��Q.�J;t�B��W���T���W�؆�x2��P]T�����4�L�?R�|[��i�\>}�TS/�;S��p���R�6��s��ʬ�˃LU�j��1�(/�!�.��7������MFb�#f������VG%�8�]��ʶՇ&K�!KU��2����7�,�B�=U�/��]�T�wH�x�[vw�P����h�a�'�c���xr^�Dss�7��g�N��ל�K���{7�?�\�.֯��S�5��JԿ�T��A�Y�G|
����D�1ɝA�¾U��T��=i�f��F	�A��/�J3��o��F90$��J�����~��J�@�#H�ǎr�����J���}�� �    ~�^ȶ��k�x��'Аc��y��a�lL����#��Ptj��臗|!��)X��d��CӬ0�����>M��|���˝5���6g�W��kXV-�=� }��iu$��ԙ_W6�f�+K4�hӐ��A儊ѶI)wtf-�5qu����go�\� ���d��<��;�� *Z�Ά�F�؞7nLL�Y�s�E�¸��Uj�!��dK>���k�ߐk�D�w[����V���ޢ]���[l*9�n���]�(	6F=��#�[-�|�(HEZ!rH��P(�p�5	�lv�޽�v����2�h"j-�t�o~���.�a�U���Y!ѹ��{�Cg���Ic钊'���7k�W��: ߆�� �Ǧ����N���rh�d�����q����]{A@i�������=G}2�0�Q�*eQ����҂��j�k��hd���W_+�k��"�^�JW����3<1���Ĭ�Des:Ѯ]t�	��~HS�#-�>C��,�H
㉵3�j>矬�"J��|i�0&�c����|32��� ��+��6{���h�X*l;Ag�n��l}0��Т�o�5H�o$�C�1����0r�[E݂C��GI���=c��*v�L�j�۸�w��G��u��+|���T:��0�������	�WP	���9�i�k�x�Czڛgy�M�r����+3d0��Wm	Y���vё3�� ��e-����n� �	��<6���,�T=<��g�~g�ՊV��4!��i�rj!طPk$����	BY��W�5����g��L�`�����>NUQ�	M�[����gf *VDV&�j��ow��gS}_�����z}a�P�-�~s��Ai�:?W�/��^��?��b�T���:�(!fJ��Ǌ�loow���n0�tq0�(o Q�j�0&�bm�h+i3AH5;�T���vp���^�`1P��(��x�� �����Wc����K #zƆ��[ʂ�V��8��e���O�=X�AB_l� ��]��e�g�gLD�?��Z��|N���|�zğ6�`�ݪ���.վ�}�(z%��Ye����3�������q!�h���I�㉉��(�����M��:`�d�:�A8$��#X�j�^֩=������Tv�a���_Ƶ�T(��Q�U�\�:W-�����OW�g�j�����A/���i�&��*seX�x��0*ϤR��m~��d�:�go�1�"Ս���J)M�C�ղ��!z`;髼3U����U+�0!n��F??�+G�ePڒ|����d�ө�����!K-���� ��b�mhj�&L]�H.�K3��˳K�[*U�}��9�~/6��!˰�	�7�]*׮�>��Ef+{���T��2%mKi�ӹ�G���H��4�UKh����bb�E�G"��Vt�v��a�Tt���F��2���"�����F���>k�-;9��i����tE����h^AjK�/z&Y��G0-���,��ێU-�c�jC��($�&�#UXy��+�q�-���tw�Y�d�B�5|�a0#��M$FJ���e�e_t��`Ǒ��T�7<d9Z~�h��Q~ݲ �a�a��En�I�f�S�u��#��[�b��J�O\��;0�T��r��cث�T�n�8�Ё�A6,^��v���@{(ўY/w�@�B%dK��<��ˀ����������~R�_e��Fh"�l�|+��~��oessԖ�����O�j���I���,Z�O��2R���)��w3/�������aM�x����=>���-VpCRl6	�PFx�2جR�Ô(�.�>z4MN�S��ЦN��ԇ��ꟷ�������[��{O[ز?����)tg��v����n޷���vp��ezA��^�<�v�XǤUo� !7�e�|���F���"�������/�?�~�T��<^$f�k>U�I��*���I�zxt��R��j�7P��D���CF �K�2���@&ެ����n�7���0���`�g̎�H�Dzfc���m���f���{���`�"资D<=L�C���9�W�R ��DM��PZ�y���
u�u��e`��[�vGM\��x�)���3��s
hܨ�<t(�Z�Vp+WY/:h��+D˨n	WgKe��tK����i�.�N~-Ѯ}=�a��͒�g�eM=�����"��n%�s�<su�d��M ��-7KgT�q�Z�2WN��k�߀U�����2[�J���/i&f��X����s(����ר�ppMS2�um1�o
�m�Q�fҷi9�}a�˄ �{�k�(m��̤���ꆮ�u)k���K�$ylmIv	�mw�3�]>�(� �sãʲ�1�XP?S�_dn�p��Lmp�t��[*�z1�C<�b�^��'��[_�y����g����^��yg��H��ӥ�܇�g���S�˟{��g��:���끝�h�V^\��d\_oʚ�DRR��K�aw��I��z����G�U�W�.�z`�2<�"�x�Tq�i`����"�S�޺��)�X��y�@i*�a��+�)��~.յ4o���h�l��?M�D�SA\T�����Gy��}�Lj�}��Ϟ[���6�nE�;��V�o�����)�i��O�C�IP</Bw�+���n,P>i3c�"^nX�RѲ���F�
�.3�p�j�V+�� rQ�k�5�l�󆶛�� $2ޗmڠ����f������&�L�#D#����.�|U�@����>��Y�+ۋ�J��1�o�Q��yL����x�(�	�!��9�~<�-�nC���!�v�+��L}1i"�!��d�:kI�m*7/��FӎXh�T�)�+Q��m�1�7�ԩQ%�%Vxܤ��}��n:��G�Z�b~��/��k��:ޡ���аF��O��{t'M�"���$t� Z������T��氁쩣����\Aϙ�鳩˖�){%�A\5��G���[-�7���Y3Oz�?6�F+����Zs�M�����Jc������� ��n�~��^��'�Ro�U����0���N��Dor�~19��[E����m<�v9�F�
*JnGh�1>]��	�7_z�(x�X���/�MX�p19}�j���b9��ąf��N���isw!���	�X�Y�|7���ҝ񐦕��L�����R;�b������f�1/�np�����6X������3�'�R�4»�J�=��"��s���Q�B�Bu��I�!���A����Ee,�.��d�0<�b��U���6��ξ:t��������+���@?|��BȀ���?[��8�&��5p����>��␦�<6T<>r��a�~o����-q.��?�@�f��㉌rtiNK�;����?�Eo�D�� #"Gy��]}�nq�CI[�R����$P���_g�g|���Ѕ��S�r�a�v��n��>�'0,�f��qZ\���L~>_�H&����:_bH�|8�c$"�ß3�Z�dR,��;~`�^+��%��J����8�_
�S�Z|�ɽ�t'�SP��
�.�vh���%a�79E�i7�ۊb8�}/�����=�F!0�>���q����ƒYz�V��+G`"���?�g���ٓ�����GA8���FR"���9�)�ȃ��$�	v>�K�
�-k��:���x;|9�1�("|����~N�4h_��Ju���wF���p>	B$	E_�r6�jr&������ULJ��Lh����ܔo,
�b���4>8�c�6��$<z�!�ue??���'Xד�CC�@��Iֲg�Dl48�Xd�;�ϋK��������h�dN,�
��PF�"�.C`��4�f�pbop��i��L�XG[q���C�y;q�i�"F�e�ĆW�g�G������ZSvE �й���oꕓv޹gs��q�!�ѡ�c�/c�>oܑ�gg���a}�c�
Lx��m�PH����9.<�r�S�נ�t!��s��sO�r-�&h�u�����l�)�JG�=���ܡ�B嘳H'������H�k�����ɚ �B��k�    ��P2������e�T��r�<���Uk��؀!aK1&�u��?ұg��1��-�t@@��G�գ�[*�j;�DB���͔�N�Y\V2�j��_�������r[����bLݫQ����������ͧSf���+�w�[����8N��|�г?�F�aM�)�5~���Dw-�#&����O#���o�$������,6��qR˱%0���qX��x@�R�G8�
��]2�60��}�� C{�Ԙ�ݕ�D<�{��N	�M:=Xh���0PF�)��$�p>SC~���G����s@�}_�,�fT�uӴ��E�"7g;�A3I����i��Jr�˴).L�����1���㼅L��|��3W��Q����l埜)P��.��������N���}���a�jqx�8u}�^�f�0�O~�F�>�u�W߲P�1k��ă5؊��d���iX�ֱ8&`,f��ag��,"�T;	��8�^��1�%��'\O�A��:���Lf�G��T�[��T[�3�y��Ć'�����{x �
lI�%*���K`�rGHb�%��S�miP=���Ǥ��M  n�\YɅZ�*3e�}���JK�+���r`TI�:�e&=���Ȑå^ͅI`dR��}�Z��*�C0�/y�V�w�F��/Aa|ꩄ��	xO��;�)��Aan�C�b,1�/lSl�&wIa�[���[y�w����ےz��N�ތ�i����s�rɾ�o��j1�ʊQGm�Ť�'
�&��~�7�y�.��ؐ����j^o����"unڣ	iIK����Rj���xB�a�������˚�u���y��^E��/Ek�aj�7�RU��إ�x�p�э�+^�z'�����f(�NWke@��͕g ط�����c��D����4�k҇ Seu�J�z��Fl�C�^�u2��maQ���]��#�S�Ǆн��綎1J\�:�j&��A�;XγYNT����\���p>��HJ94F�/)��yΙ�kyV8&,�3�V�u7I��퐾/�-"��v�U��w���������μ_���/q/�_�G���)���S�ĸ����6~�A@�o00�B��" b҇�8�v "��+�״����=��a���sM��h��`���P�F䬗Oh ���c�7W!�I`+�܊%�e9���:�T�����l�Ԏ\S4���'��.��N���t�ԾB�P�������� �99cT��t`0~�����*(a&5"j���V �weT��v�U
���]������z���Qw9	�G�!��[x�}
>����]�Ā&i���yʿCm���<'l�lX�%Wy>��R������B�͑��/�`K�N)v�R���n�-Eq��:���}��J�Cbx��x���j�d�G����g��!-�ߓo�h�PP��W��X�#�~IL�4��c�@���������T�ղN�c��
�r�Q��p*�+��i3��������æ+����'�+���z�Bf_��;jD���)D2u_�6����%@�C��R�f�%fV���������2��v�J��磙O�6�m{fL�RmӊS�d�t!����5a�l����#Na�g�H��M���e���M?�n�F5���mӥ1I 12�98:c��o{�`ɠ�N3C��J�wWx�t��R���߸' \8�T����n�9avR����<䗳����O�U�f�� �;Q�0��x���k��ٹro@�vW(7�؅�f����u�S�z�N���H���j�Y(]|srJ�����/Կ��p�4��$t�P�V?"�[i������&7ΤG�\,]�T�O�[Hο>����g
���]�5��A�R���i3k�Pa�H4S�ܗ疔�]��?A�q_�BLR2e�o���Z�����\���C���[��c��'�T b�*�|Q|�zaj��t�?:7y�JX��,A�{H�����^P��8d�_#�I^���Kg�U�4�.��w]�_uSm��/lY�ΜR��ͯ��+^C O�v5�Cy:��S�@��?'�
d��b��,�-��Mu�7`q��f�`P$����݁�'�)�-�യ��vh�"{��3��d~'�����$��G���g՟|���_�̗��[���G����c�'����ȼh�P<��v ����d�"�"z�s.ϕ"x@�IX�k��A�`u ��fF���4�R�~!�t�i�!2�6	Zʝ3��}����4��fM���]�� �ށ�1����{��g�4�#˕  k$p|b�6�p�����Ç\c�,�.�t��(�͋�P�z	��4L�8� 7k�M��0y�ٮ��l��ctq3(��\t��)�dB|�5�0�X��MwNt �P�`�N��vR�^^D�h�t	�ǿ�>?�6&�a�[?�ΖߜL&�+�P'��Z�t�Z�6��}��4y�2S��*��r�V�Q~�ze�^Z�:9�k�����"�e�K���?S������4+	?�A�^.[����KKs��U��U�d�^�VU_._�����O\1g5m�A�`����NW?���hy����&"�+/"�`��o�ߖ���]J�3���>��1\[���O�N��vI����1�	{��.e�5���>�[�s2�ӌ,P�0z�j�co�|��g8Dr�N��׳ͭ�dN�M�D%XY�,�Y(s�C�еRGv�,q���e����82,J���q�稥���<'��.����rP\`02l^�c��2�
����b>�����b�a��K�C�G��_�T���[ �X�B����l�GMErR��P�U9��K�b'�� Sj5h��6-_�����JFH�m�U�,����M�S촙�)�.���-,n�]��.��'d[|��l�Uj��g�M|��w�f@9�Y;\�ĳ�Q4Ңz��)���9r(&����t�|�R�w�d [�Q6�e$�M�i4h{j���W�Ȗ��Ƚ
/YJ!x>Q���l����y)$���\#[����� 5�$�#6�v���8q&M��lJ�x�����/h���~l"�j�^����?>�"����gf��-�IE��� e�(�^+͔kեz�YK��3��㷃��@�	��\R���a����ԿdI��� 6�)�I�r�l4���u��]��\����pmG�	��f�<H&�z�B	�w%~~z?[�fּL��ȇ��-��,�QZ�U?����+�"�ouvSi���7�428�e���K���V8'�'|]��q%]�*Pw>}�p��el��1��2p�����J��7��?�R
.�"Z]I�\�@�mWx������8qK�}n�_�Υ�r�ҥ�\�~=gڌ�S�(a��nPab�\�O�߮�G�n����h�|���ު\����+-��5ʥ��ՂxY���p+�5����uu�poc~��kU-�m���8� �N��Bl�a壠��PF�@1ר���#�8��e�K��&�g��n����:��=�ߝH��+6��dx�Ǔ�ڕ�i�1��]�߮Ԗ��/)"[Q�?�`ގD���k�P��x�!���eՑ�Q}��*3�_�.��?]Z\,��� BP���!M�j��z�:˔���9"�)K��7���L���we��dYY}����ۉz�Leq��A�v��KsSg��9�[�)��c:��%��|���hXs��Z��8�M]�� [��͆����BxnB�{�_&����Z��|4]�A�"������f`�9g����%��9L���n��\���`�\��;%˚��X�/8c�UT2RR(l椚�F���8j3��h4��L�nr�<P��1������W7鸔�O}�ka18�g�z't~έʎT�I���~�%�nEYL��\@"&�*R�������C����\r-�a�Э��)��qH�|��ˈm��a�#B�����(�!�!���{�y�Q�gC>d����M�KyG|HX��iAɨ�T�^�R�����q�9=<L3�
�b�*�_�B9k��3�NP*    颽�M^��&K3�L0�Q`�2�ֽ9���'�c�X���ݪ��M�$�;+c�e�D��N<ӄ3.�|�2gZ��5сP���Xщ`�S�x�AGG��q^��x��e��0��6�c��_�9ŧ%X�˙��kGj��2\�	<�pT�Zmyp��f�J
=��5�_eR��������c���1�!��h�6��3���l�C��'���t��H����
�5uA;��S�`�%Wj���~H�����k��8	�R�&_4_l�	�4�y5���5�X���1��P�mxen��HgWH�AZ[�{|� /�DD,�o���aG��߅�Rtg�C��8YL�m��+�ӡm����I��"|8W��l*�TXt*j���IH>�E٤�tm���5��S�xz۝�rv��~96}���x7�2h���jt�(����UC�O���J@�`'Fz��N���㣣<;t����E��8�H��x�T!�Í.�cI�
�ϵ��	�j#�N� H6�`��X�B`l�f��2&�ڂ�V
��iv7����>�֛_�'���<�����\�4SSf�w���o?7���M*��E���0���n�C�.��9s�g��;̴2XۉF�J�B�/�uO�7Xq5�ékp�~��w��!m6:%���6��ߚ'�i�&x�"w۱���������A����'�|��mh�0)Տ�&e���#� HM�[	��~�3j�؃]\��4��{�٬={p^���*��`�HH3�arY,�U�-F
2�U�ڇv�`�� �B�k��L��Qٺ��ֹ�v���̶�B�
pq;9ƽ��Ӳw�+j ����1��^���z�.������	�x��=��Ǎ�0n쒣	��O�[�**��nOŌы�Fi�Z]��|���b��su�1%���V�R��(����Ⱦ�7Y^ �5@�Q+�윛���q�������M��� 0 Z:ź�a�`�Eu��yr����)��5���\���y5*���oN��mE7	Ip�G��|u���R])�?��%"�/������s��e{��%�d���J�R=I�����X@h&�u�N��m����T�!7J��n�~f���Ejn��R���.��Z�Zi�����G%��7�_�H�(@�(|͌��s94��A;��F�L1�4�N� a��QDp�a�����|���>` ��47�,*{�}p�{ɶ����$.'��mUD����k*֡^?��t��rj�:�R��Ɓj<ح��I��ƈ�GZ_�VK��f����A��#�Urº�\)2�%�a�;}}�a�zF�)g�M]���&��)=₫]<mSkg�
�C0F'(�����0,�a@L��38�2�$E�I�۠����5���>�To���50^f/��`6c�C?hm����&��ϟ��e��q��w]�3�u�r���:�4w@�V�wjq�Q���	����}jߵrY�^t��B8t��.G���K�GD�Dx�Ƨ���t��E��A��N�����-t4�9��#�A���96_ uիsA3�(��o��[C�jqj��p�k�gl�Rt��SPz��YGu�̥��I��B��2Uȱ�D|�w0&��B��g��G�QHg1f.���t�d��_=�dݮa�Q��O��w����d'�bN�[����E8P$<�}���E�ef~�8�"T�#C��������q��=62:��s���|e��0ob�^f�-��d������#J������/�}�xcx��m	ip��.�B��ҝVĝ��@7��T�پ9�+��Mi/�z���r<I��������zS�׮&��$�CC�#(�*�c�eb�6��6E��/��(s�ˁS�im~�=�aEf8��Sx��T��3��H�u��@h����K6�Y���pX��nCN�ل�'���[P�^ӄ褅-?��3��9�Y�V��a�w
��U�.�yj��hq���f�zB���bT��B �ȸj �JiO�avY� R�\;�3�-`lJN+{�Bm��,k���<���f�<q�P�VF�m�qK2;������*�Nu�g�3�q���x2[����Q^L�l&��S��a�R�Hh6�)kq�T\+��]AC���\�2P�Z+�f��������̫.-&����"�G������&��I�-�:���W�L|A� �T��#���ޛ�-}�u�����ҥʵ�
�fH9��(�_�H��P��5��:�A�,`v=��Mn��)��HA��p����Z.�1]���|�#0Qgi���$l�-�V{ w�.�v�<黀B#s���qr��;�;�n���R����<�F�0H�\�	��n!�r��,�^Lg�>Ǽm��줆��k����B~I��a,�h��l=�޽��>YǄ_��?�������۷�R��&.0U��n0�P�q����ˡ��c��npDi�M�����1pB�q1>��@�2��,��%F����ɈhO!���� �=&����ng�6��n��&��K5vdɕ~�+
-$�N��W�ND1Nd����i8���U��N�}�:w}�:[BwB5j�R����6�|���v�����k�LCwnv	�d��4$���-h�ױ1�Xv3��8��&�2rM��z��#��M�Z�T��)y������t�$V��E 4����ΔL�C�R�=H�ϐ]�]%D�y�Ke��+�&���A�]�lX�Њዦ\U�(ws���?�	��S�zU��Փ�fq01U-�~>�u<���ϓ��t��kg!�ʱ޺�c<�����??�w�u;��$����PH��!(e������܁M?��8'���5jÈV��j��Ր�K�i��7�H��B� t,J��ҵ�ɏ���V<�ܵ�U;V��b3���-˨����7�R��q�4�-y)َ _@�-�T����io�H��m�uJ�֒+�u���v4
�㙎4���&�u��@[@z[2cF�]�\#'�Gn��j�H�Y��5�ʁ0���m�c��I��)�K=v��u��Zy��;�*�=�\ |�(R �	���A��a��vFq8[�O��J���������h�k������>x��Wa�[qy�҃A2��e���w73�.zB��FF���V���{�f�X��#�]K5Ӥ�1Mh{b�R�|�|z����Or�^_X/��sq��\�pv���ߜ�t�㽳�����YX���l��ܐ�0�
ĺ���dO`�7�1qe�'d��˪|�ED��^���/�p$���z�m��޺o��zi���^����Xm=�0��A&�?r"��o�"�Mb�˦]ӆ�>'��I��V�u �8?_/_���՚�u*4�M��M;�F=�+Z�Hc�Xo	Ǡ�ݥ�������]�!�������Јe�Ԓ�83f�v`�	s�=��'euB��ԝ�RP�7��X��&g�~;9����.NL�s��(��6[�7��p��V��Y���5��o��c���wԧ�����~/��:��4iN�?��=���zz���ͭ��w��=��C�塺���~�V�p͇��{,m�C	~�KuBֳ߫W��s������� �W� ԣ�õ�Q��}��o�p����uC����c��p�uäg����(���g�k�3���A"�2Cyh�P��=�,��u�6y���h�o�q�����g[R��?ұp�������qa-��1g؄<��Q�P?逸~�������N{�O��9� ��0!��n8��N���U@��M8�x���s�s7tHA��2��s�s������z�Mst�Qe9}��yq9��{��mD�5Y����%X9+;�������l�o7ɔ�¿	�����{��r�?��byn��b:���΍Y��k��(��R���^w��{�����d-�D�7L��r��R� M���$�Nz��sQ����>�w~o��|)O>����G�widX�/|�ݱ6�c{T�    L�������H����}�C��7��$F6r#�Y��~�&�����%�xg�2,�X� U�S닏%ez��Ǵ�&6���h S���ɨġ-�A����؃���#�q'X�<ȃ��t�63���o��X�V��w�F+�����rH��N����k�K��f��]*����lN�C�Է>|��XOJ��K��l�Z
�'�|����m�٣\��&ȮD����{�Vm���L7��nSC�MR4��Ui�Wn�ߏ����|�$7�h����t�c�0,P!c���놧�JQ�e�ԇ��פ�	3O��b�}5u�~�#�
u�B/�	�/���#�	"q��� x�#AID�V�.���ql;�E�?u=���΂�(�2G�!���݃��>6�G���`5>�A8^ߡ_�@��^���jk�4Ռ 5�9(	[�����W���L~>r|����l��t ��vlh"9ӆ��'��or"�l��9jDTu.:R�ͶI�t1uK���*�9��A�q�(M`��|N�ul?P�-:�O��VF�N���}���ߠ�8n���O�N���(�Ч��o�����'�[t�	����m}�N�_a?��-��:�DL��
��K0�P�x����&�Y(��5�ڙ��2��U�ǎ�2E3x�꿜3��0�:�7�E�ދyɎ49D>߆48=+�o/{��{��b�?�YٌD���q~�"��s9׊ƨ52�(-�@�X�i���C愋�U]�b�t�C��v{u���<T.�cm���+:I���}J��u�W>�i������x���P��A��j��N���ب��np�������2l=0&��>4l������gVR��s�a6dV�IA�'�����R"���H��r��ؘ}����aVϋ`��o$�X�:n9�m�23�.�ȠS���NH�n����i�D���('}�J�ҩ#Mf�; �R=�*�0���2.��`4�!q0��i7����{1�������o��Gn#y\*J&�gk��l2����.�c��ё���G��g Rі�N�6���=�cM�*�u�Ja͚���6���
Y�2�i�@R�
yJ��B�m�?A�
S����}/���d�o�`E��%�j���%�.�������y.1 QH�G&��Z�s��7��	1E25]��V�>W�O���.$}�@���7�&?B�������<P�^�V�A㿹zr49S�r��\,��]��K��F���&�Q^��r����%ힸZ̀�&'�]_��yM?=������/�jX�k�ۨg]4-�9��ѣ�K !=/t����~�	��?�=�qߔo�w��5�hn� s��̮O�<ݠF��v:g�[�����n[x��"���h�C��7��i��kW��<�@�c��%��I�k���a�Fqh��{;��w�ȢU 9YKN*��EFc�}�i�<��d��5v�A%a���odV�1�$GY�5�
Kz3s�*3L�z� &#�A�|�AU��6~�נ���lL�N��^�����r��s���n���c���`,ǆ��g���,���Ά����X��(�ZB�)�}�2�'�y�m�vX���l0�F=�5��N*��+��@OK}1���_�3$xPa�2�ϕ���I��[5�����A��p:;�M��5Lh���vS"mxE�,S���-�W�"�;5;�z7�ehtC%&q���C6����>�og	ĖNf�H8�}�_��=����M�;#�pì<G,t��3��g�ˣ��.��(jjJ���*�5�yi�2�
gz����.X����� �!�pK�����'�~3��8['Z�_-º�WU��������-1e��2�y�ўt��LX䫸7*/�0�����3�W�ڒ��UR)�d3�(?�/~q�4;F[l�l�_1�٤��L,,,�T�/��R�TYZ��+�N[�^ℱo�s�I�Y?@�M��F�^�E� 9WE�ڍ�8��^w�u� ����y��Y3���Aq]�ˌ�X#�I�ڻn�}�->Û��Ej�9IOJ�INZ��h񉽶		*��7da[�Qa�n��К)���m	p/t��%B,a��@̉	�9����e�2���KM�2M���3���%o�Ek�(�t]�	_v�C�.�`���\��'��{2�m�
�T�Ex =���	���ų�a��n�����S����^P�SWP�7���7�/{mו���]���
�oP{�G������z�4�ذ�M��Rrψ�o5$��V�j,���o}�L
 ���`B���r��O��ՆXwH��gN��;�w��m{e����˶x]��D�A6�&�h&q�����c��k��O����������ًJ�#Ŷe��W�RAL;�@���	G��Z����72/#�m�ݹOg!ƥB�R���Hv'�®������4��Z!�}�J��
�����1����𱱱�#��V>�s����w�ǞÖ�N(��;�D�W~�5���ӥ���:tݭ�$o�.-��u��5`�c��hkp9�n&�'v�����#B�-S���S)�#�wYS|,"@FrP�`�}����n
���/3<}�ẑ��P��c�q���|N����(�*t���l{I���eh�U;u0�ؤ��$ȩ�c/s(�`������ܦ���ӎœ!T �x*��z4��zY/�3��ks+Fv�6����3g�����L7�@Ǟ˓=�)�9<���,�¶�[D��fhS��o;�|`��IrO�s�������:���T���M&� �b>v�~�U�CB�|-�ΐ�}�HΦ�]rEi�²�us$����hj��aΙᚧ�Zh~��Q�̬�7C�cǂA���}HǑ5���-�������To ��C�Iea����^�e��>�K��T&����}�O��q�1�+V�9!�&r4�Q��L��Yf����h{��/׼=�e�8����� l{����[���''A$���jq�;�g.5�Ӻ�[@�5��BG8������eVTʊ-��Ҕ" Õбx����ʦKel�o�p<k
����)3�yG���(1G��>�����-�J.�f�W��f���4�1\���i4u��0��Mk�s�1���)�;��N���Vb�6��zp_,�TH#.+(=�%
�%���r��M"��6��F3ۭpB[N#.��dDVb���D.<]��.*G#�匙a�H@�&oLO��&��|��˷������|pނ�	[9����[�n�/��ϔ�̘�5�DwH^S*K��d�FG��� ����C�Wz��>����{f�o����1��!.�2Ǩ�g	i�|4�� Z-kj2�%k�.��Y��g�G��1 W��K�0F��J�rO�q(\�yPm��(���Ǿ4&��� ���H���^Z��y� ��b#��!��RAZ�ţ�`9��{ص}�-I�̀X��������fK?	�T�/k��*�f�M���A�U}�{y��s180�ÊL����:s;�g:gj��1����YᮒJ�����A� mI���m�M\7I ��|0럜
qa��W���y=�	S�Kd��}���J4��z�\�w�����EQr홼	��p��6Pfw`���L�^��<�n�I�^��$�ƯA}V�?3�&���1@]wQ��'��	6Z���q�|��l�F�t������-	�Y��ϒN2�GC2uFL�u��3mB4X:{2_��iº�e�=������}���&@�Gp��8j$��oY�;�¶$^�>rK��9�J��tob5/6������ڃ�<y�\u�4%'g�p�@��q硑�����&�䉑�cC�4���O~Z�V���L�K� Ǵ�9M�v�����/W�*�zy�"d�(�f��IJ����s[9��3��NM��p�k��i��]�lJހ�Z�0�t����`�hV�*�	X&���;��u��� P>����!���r{$�D��2w�݄;��;��sNp �	  i7t�7S� ����}j�}Ϡ6�U٢�#���>Q{�҅=~D+���gm��4#D�,>��c &:;9y���ک�0z"�Xq!���9�U�����$��YF*�}��G���I��^��o�D#�}�n�,���ϗg����j�z���e�����ji����-�G
�.�K�������L��P�?�6Bg�usP�iow
W~Z�!&G!�u���� ����i����R��ӥ���r��ߘ���9]��z�k�":p�߾�7��36X<��2T���'�RG���䢂�+Jن_=Q��O��ݙ՘=R�cg��<��#���!K���S�X�z����CC��=���ד�+�R�?���F����Ď-������C;["�aF����5���Rv�x;���4�lr�R��W!�Է�p
�P�oǨ��,���5Aq������@-[��Վ��Dv �#@jν�dzֹ�E���\���G�@M�T4���"#x]�m�m�s��5�HXT7�5��;������}��%��y���J=�W~ݲNG-%fǆe���w3����D�aC��=C�،Ȗ����'`��Ϻ�Ph�'�4�K q��驩��tpY~�2l[=��SH.�.�Rd�H0��ΟZ������֏Ԯ j>H�&���z�Z9��Yu�\8S��^_��V��]걦Գu݃F��W
��Wl��K��H`���(k���N�;�qm%�'�grk��������K�.X�gtcI�X�H�`K3ף�K�,"��cFp��_���w'���(�qb�t>�e���|���Hq|�]��=a3P���R��D簳l3����)��w��(fF�!:�J��^mYQf>G�Jsֵ��� O2�u�_g_O�QwN��^TC��=N���q��ˤ�mx4�X]v�!�1�MW	o*m^Q����Q��X��?~���f�ޤ�r�;�?��٥Zi�RJ�k�ٲ�K�3���b�F�$�;��Mu'�6"3xF�qL躹M�d�vjDH���<��?�yG�;���VY=����X���W�r�b������i$:^g����a�躉9�ω_s�mBA�~�t�!T2�MR���ZD.�g:g�	 k�&�48⚈�*| �O!�YU�/&�ö�V��f.����d��@V����!�I��l'V�c�Qg��22V�y������R���R$%�&: �"���s���ߐ����+Sw�x����Uy��cfZ�!��{��<��b}J�L�7'��rMG�E�ӬX�����#�M$��2�9Ǆ�^��w��֧,�k������幥���\�t�4_��9r��p�[nG�lKϚK�`3�w�@�3.����'G��	����i�j�-nۮ��`J��
i�e3a�:���a�,�}�,[K��Ig�o�Vr��%��ϣ*$�¼�Qsw�(�z���p)�^�Z�g�3Gפ��+5�6oOۭ��BHb��õ#�D�9i�j��X����0�-]�&4UT�Ft���`�a�!��f�"4�.M7�^Av7ZBع%�����ZƬ�Y�����6́D�<��	��Ajx��r��*G? Ek��5�!���=m����4�!Hn e��O7z��уx��|�Ae���86��R��1,���jjG&N����C���Xp;�4���ܳ�����^�)���`�ՙnD{9TBB��q*6l�Nm�6P����c\)��(�2K�i��[;%��gwn��7�j�Јib�>^�e��hu��pQ�
��]!n!%?�([R��A��m.�N���m.�9rX<W/ڏL����^�x5�E��s�#�+��D�	.=�KBp��f갔[�$Յ��bu�6S����	���bnt�ݰv���[�9�3]͝����z�9m�;6�L31l
PKϽT�m�Z��2��	Z�p��Q?�tn�3\8����%��,��Z�5��)jsȡ!���������J�\���6��u��m%�0� 
��L*���Z	�Z(�4�0XX�w2� �<e6�m�s	�����bJ(N*1��*n�t8�(4ڼכ�p�\9آz���+a�-�Y�<~`��q�"�����砿'�uk�Z`����2o�����c<Q�d������w��hSi��m���
�/���r=����aWS�\�̽l���A�c�A�so�N�W�oŌ�a�p�+ܠ���N��
��ni�2�5G�tAP����%�PݾE��V��C �O?Z���K<�Aok<p�PP�t�X@���\W�.����A�D�����4)/.���4��F�T��49}��ӳHS�U�֛�D������ȫQ��Ys:X��wj�fJ����Ӥ8����"$�S�hB�TP��.e;T�!�R�꯲�:+��^��*�:y�`3�S������_���+��٘      D   �   x�%�A1�uf*�.}�s���yMT��#��@���r	�Ԑ{p�RW!+Ig������Kc�Y��.5ɢ�R��:r�Jc�˝��+�����<yҗm؛*5z(g;(g���������`/���4�i)[�q�8����L�QW���OD���:�      @   ~  x��SKo�@>��
�+*����[
�$��[����Ad����R�M�K*���? �XiK���1���QQy|��f���C#j`.�<�\h�� ���,a�G�,�)��ݎ��6<
�^��V6^h�1h,,Tȇ�h��i�#c5L�vP
����j�:<K�h`,ű�l�x/fb"~��� ?oD��Ź����Q�]���L��� >�	�g���#�g��N�{�����y,��݄����dX��lX݊
��(�����h���fD	��Ļ�~�+�jı ӴP6q�B�'}eĘ��-/W
�@�"��a���(��ѭ�fG%6�����?r����RW��u�4�l�)6q-h�ѮlҖ=�ʙd��T����u����aO3x6�O(&
�������By�$��x��(�:z6�����zF��+��y���j!�w7&uA�����1.��9.��0�7�����ȷ�>��%�A����ϯ��r�YA
�˿
����~o���l�N'��K�P��2C|/�fx:�n�wukؙ{mm<�R��2ׅ���170nV����D��&�=���K��(o�:5ϪD�4�������V!�Q�-���C-�e�ro9�:x�r�'}�H�X� *     