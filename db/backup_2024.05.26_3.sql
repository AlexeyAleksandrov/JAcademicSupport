PGDMP     '                     |            AcademicSupport    15.2    15.2 R    l           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                      false            m           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                      false            n           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                      false            o           1262    34259    AcademicSupport    DATABASE     �   CREATE DATABASE "AcademicSupport" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Russian_Russia.1251';
 !   DROP DATABASE "AcademicSupport";
                postgres    false            �            1259    34476 
   competency    TABLE     �   CREATE TABLE public.competency (
    id bigint NOT NULL,
    number character varying(5) NOT NULL,
    description text NOT NULL
);
    DROP TABLE public.competency;
       public         heap    postgres    false            �            1259    34290     competency_achievement_indicator    TABLE     o  CREATE TABLE public.competency_achievement_indicator (
    id bigint NOT NULL,
    description character varying(255) NOT NULL,
    indicator_know character varying(255) NOT NULL,
    indicator_able character varying(255) NOT NULL,
    indicator_possess character varying(255) NOT NULL,
    number character varying(10) NOT NULL,
    competency_id bigint NOT NULL
);
 4   DROP TABLE public.competency_achievement_indicator;
       public         heap    postgres    false            �            1259    34763 )   competency_achievement_indicator_keywords    TABLE     �   CREATE TABLE public.competency_achievement_indicator_keywords (
    competency_achievement_indicator_id bigint NOT NULL,
    keywords_id bigint NOT NULL
);
 =   DROP TABLE public.competency_achievement_indicator_keywords;
       public         heap    postgres    false            �            1259    34475    competency_id_seq    SEQUENCE     z   CREATE SEQUENCE public.competency_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 (   DROP SEQUENCE public.competency_id_seq;
       public          postgres    false    221            p           0    0    competency_id_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.competency_id_seq OWNED BY public.competency.id;
          public          postgres    false    220            �            1259    34742    competency_keywords    TABLE     p   CREATE TABLE public.competency_keywords (
    competency_id bigint NOT NULL,
    keywords_id bigint NOT NULL
);
 '   DROP TABLE public.competency_keywords;
       public         heap    postgres    false            �            1259    34626    keyword    TABLE     S   CREATE TABLE public.keyword (
    id bigint NOT NULL,
    keyword text NOT NULL
);
    DROP TABLE public.keyword;
       public         heap    postgres    false            �            1259    34625    keyword_id_seq    SEQUENCE     w   CREATE SEQUENCE public.keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 %   DROP SEQUENCE public.keyword_id_seq;
       public          postgres    false    223            q           0    0    keyword_id_seq    SEQUENCE OWNED BY     A   ALTER SEQUENCE public.keyword_id_seq OWNED BY public.keyword.id;
          public          postgres    false    222            �            1259    34646    rpd    TABLE     r   CREATE TABLE public.rpd (
    id bigint NOT NULL,
    discipline_name text NOT NULL,
    year integer NOT NULL
);
    DROP TABLE public.rpd;
       public         heap    postgres    false            �            1259    34782 %   rpd_competency_achievement_indicators    TABLE     �   CREATE TABLE public.rpd_competency_achievement_indicators (
    rpd_id bigint NOT NULL,
    competency_achievement_indicators_id bigint NOT NULL
);
 9   DROP TABLE public.rpd_competency_achievement_indicators;
       public         heap    postgres    false            �            1259    34645 
   rpd_id_seq    SEQUENCE     s   CREATE SEQUENCE public.rpd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 !   DROP SEQUENCE public.rpd_id_seq;
       public          postgres    false    225            r           0    0 
   rpd_id_seq    SEQUENCE OWNED BY     9   ALTER SEQUENCE public.rpd_id_seq OWNED BY public.rpd.id;
          public          postgres    false    224            �            1259    34785 -   rpd_keywords_for_indicator_in_context_rpd_map    TABLE     �   CREATE TABLE public.rpd_keywords_for_indicator_in_context_rpd_map (
    rpd_id bigint NOT NULL,
    keywords_for_indicator_in_context_rpd_map_id bigint NOT NULL,
    keywords_for_indicator_in_context_rpd_map_key bigint NOT NULL
);
 A   DROP TABLE public.rpd_keywords_for_indicator_in_context_rpd_map;
       public         heap    postgres    false            �            1259    34790    rpd_recommended_work_skills    TABLE     �   CREATE TABLE public.rpd_recommended_work_skills (
    rpd_id bigint NOT NULL,
    recommended_work_skills_id bigint NOT NULL
);
 /   DROP TABLE public.rpd_recommended_work_skills;
       public         heap    postgres    false            �            1259    34260    skills_group    TABLE     �   CREATE TABLE public.skills_group (
    id bigint NOT NULL,
    description character varying(255),
    market_demand double precision
);
     DROP TABLE public.skills_group;
       public         heap    postgres    false            �            1259    34309    skills_group_id_seq    SEQUENCE     |   CREATE SEQUENCE public.skills_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 *   DROP SEQUENCE public.skills_group_id_seq;
       public          postgres    false    214            s           0    0    skills_group_id_seq    SEQUENCE OWNED BY     K   ALTER SEQUENCE public.skills_group_id_seq OWNED BY public.skills_group.id;
          public          postgres    false    218            �            1259    34313 *   subcompetency_achievement_indicator_id_seq    SEQUENCE     �   CREATE SEQUENCE public.subcompetency_achievement_indicator_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 A   DROP SEQUENCE public.subcompetency_achievement_indicator_id_seq;
       public          postgres    false    217            t           0    0 *   subcompetency_achievement_indicator_id_seq    SEQUENCE OWNED BY     v   ALTER SEQUENCE public.subcompetency_achievement_indicator_id_seq OWNED BY public.competency_achievement_indicator.id;
          public          postgres    false    219            �            1259    34268 
   work_skill    TABLE     �   CREATE TABLE public.work_skill (
    id bigint NOT NULL,
    description character varying(255),
    market_demand double precision,
    skills_group_id bigint NOT NULL
);
    DROP TABLE public.work_skill;
       public         heap    postgres    false            �            1259    34267    work_skill_id_seq    SEQUENCE     z   CREATE SEQUENCE public.work_skill_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 (   DROP SEQUENCE public.work_skill_id_seq;
       public          postgres    false    216            u           0    0    work_skill_id_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.work_skill_id_seq OWNED BY public.work_skill.id;
          public          postgres    false    215            �           2604    34479    competency id    DEFAULT     n   ALTER TABLE ONLY public.competency ALTER COLUMN id SET DEFAULT nextval('public.competency_id_seq'::regclass);
 <   ALTER TABLE public.competency ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    220    221    221            �           2604    34578 #   competency_achievement_indicator id    DEFAULT     �   ALTER TABLE ONLY public.competency_achievement_indicator ALTER COLUMN id SET DEFAULT nextval('public.subcompetency_achievement_indicator_id_seq'::regclass);
 R   ALTER TABLE public.competency_achievement_indicator ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    219    217            �           2604    34629 
   keyword id    DEFAULT     h   ALTER TABLE ONLY public.keyword ALTER COLUMN id SET DEFAULT nextval('public.keyword_id_seq'::regclass);
 9   ALTER TABLE public.keyword ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    223    222    223            �           2604    34649    rpd id    DEFAULT     `   ALTER TABLE ONLY public.rpd ALTER COLUMN id SET DEFAULT nextval('public.rpd_id_seq'::regclass);
 5   ALTER TABLE public.rpd ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    224    225    225            �           2604    34615    skills_group id    DEFAULT     r   ALTER TABLE ONLY public.skills_group ALTER COLUMN id SET DEFAULT nextval('public.skills_group_id_seq'::regclass);
 >   ALTER TABLE public.skills_group ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    218    214            �           2604    34523    work_skill id    DEFAULT     n   ALTER TABLE ONLY public.work_skill ALTER COLUMN id SET DEFAULT nextval('public.work_skill_id_seq'::regclass);
 <   ALTER TABLE public.work_skill ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    215    216    216            `          0    34476 
   competency 
   TABLE DATA           =   COPY public.competency (id, number, description) FROM stdin;
    public          postgres    false    221   m       \          0    34290     competency_achievement_indicator 
   TABLE DATA           �   COPY public.competency_achievement_indicator (id, description, indicator_know, indicator_able, indicator_possess, number, competency_id) FROM stdin;
    public          postgres    false    217   :m       f          0    34763 )   competency_achievement_indicator_keywords 
   TABLE DATA           u   COPY public.competency_achievement_indicator_keywords (competency_achievement_indicator_id, keywords_id) FROM stdin;
    public          postgres    false    227   Wm       e          0    34742    competency_keywords 
   TABLE DATA           I   COPY public.competency_keywords (competency_id, keywords_id) FROM stdin;
    public          postgres    false    226   tm       b          0    34626    keyword 
   TABLE DATA           .   COPY public.keyword (id, keyword) FROM stdin;
    public          postgres    false    223   �m       d          0    34646    rpd 
   TABLE DATA           8   COPY public.rpd (id, discipline_name, year) FROM stdin;
    public          postgres    false    225   �m       g          0    34782 %   rpd_competency_achievement_indicators 
   TABLE DATA           m   COPY public.rpd_competency_achievement_indicators (rpd_id, competency_achievement_indicators_id) FROM stdin;
    public          postgres    false    228   �m       h          0    34785 -   rpd_keywords_for_indicator_in_context_rpd_map 
   TABLE DATA           �   COPY public.rpd_keywords_for_indicator_in_context_rpd_map (rpd_id, keywords_for_indicator_in_context_rpd_map_id, keywords_for_indicator_in_context_rpd_map_key) FROM stdin;
    public          postgres    false    229   �m       i          0    34790    rpd_recommended_work_skills 
   TABLE DATA           Y   COPY public.rpd_recommended_work_skills (rpd_id, recommended_work_skills_id) FROM stdin;
    public          postgres    false    230   n       Y          0    34260    skills_group 
   TABLE DATA           F   COPY public.skills_group (id, description, market_demand) FROM stdin;
    public          postgres    false    214   "n       [          0    34268 
   work_skill 
   TABLE DATA           U   COPY public.work_skill (id, description, market_demand, skills_group_id) FROM stdin;
    public          postgres    false    216   ?n       v           0    0    competency_id_seq    SEQUENCE SET     @   SELECT pg_catalog.setval('public.competency_id_seq', 1, false);
          public          postgres    false    220            w           0    0    keyword_id_seq    SEQUENCE SET     =   SELECT pg_catalog.setval('public.keyword_id_seq', 1, false);
          public          postgres    false    222            x           0    0 
   rpd_id_seq    SEQUENCE SET     9   SELECT pg_catalog.setval('public.rpd_id_seq', 1, false);
          public          postgres    false    224            y           0    0    skills_group_id_seq    SEQUENCE SET     B   SELECT pg_catalog.setval('public.skills_group_id_seq', 1, false);
          public          postgres    false    218            z           0    0 *   subcompetency_achievement_indicator_id_seq    SEQUENCE SET     Y   SELECT pg_catalog.setval('public.subcompetency_achievement_indicator_id_seq', 1, false);
          public          postgres    false    219            {           0    0    work_skill_id_seq    SEQUENCE SET     @   SELECT pg_catalog.setval('public.work_skill_id_seq', 1, false);
          public          postgres    false    215            �           2606    34637 L   competency_achievement_indicator competency_achievement_indicator_number_key 
   CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT competency_achievement_indicator_number_key UNIQUE (number);
 v   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT competency_achievement_indicator_number_key;
       public            postgres    false    217            �           2606    34580 F   competency_achievement_indicator competency_achievement_indicator_pkey 
   CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT competency_achievement_indicator_pkey PRIMARY KEY (id);
 p   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT competency_achievement_indicator_pkey;
       public            postgres    false    217            �           2606    34507    competency competency_id_key 
   CONSTRAINT     U   ALTER TABLE ONLY public.competency
    ADD CONSTRAINT competency_id_key UNIQUE (id);
 F   ALTER TABLE ONLY public.competency DROP CONSTRAINT competency_id_key;
       public            postgres    false    221            �           2606    34639     competency competency_number_key 
   CONSTRAINT     ]   ALTER TABLE ONLY public.competency
    ADD CONSTRAINT competency_number_key UNIQUE (number);
 J   ALTER TABLE ONLY public.competency DROP CONSTRAINT competency_number_key;
       public            postgres    false    221            �           2606    34483    competency competency_pkey 
   CONSTRAINT     X   ALTER TABLE ONLY public.competency
    ADD CONSTRAINT competency_pkey PRIMARY KEY (id);
 D   ALTER TABLE ONLY public.competency DROP CONSTRAINT competency_pkey;
       public            postgres    false    221            �           2606    34835    keyword keyword_keyword_key 
   CONSTRAINT     Y   ALTER TABLE ONLY public.keyword
    ADD CONSTRAINT keyword_keyword_key UNIQUE (keyword);
 E   ALTER TABLE ONLY public.keyword DROP CONSTRAINT keyword_keyword_key;
       public            postgres    false    223            �           2606    34633    keyword keyword_pkey 
   CONSTRAINT     R   ALTER TABLE ONLY public.keyword
    ADD CONSTRAINT keyword_pkey PRIMARY KEY (id);
 >   ALTER TABLE ONLY public.keyword DROP CONSTRAINT keyword_pkey;
       public            postgres    false    223            �           2606    34789 `   rpd_keywords_for_indicator_in_context_rpd_map rpd_keywords_for_indicator_in_context_rpd_map_pkey 
   CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT rpd_keywords_for_indicator_in_context_rpd_map_pkey PRIMARY KEY (rpd_id, keywords_for_indicator_in_context_rpd_map_key);
 �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT rpd_keywords_for_indicator_in_context_rpd_map_pkey;
       public            postgres    false    229    229            �           2606    34653    rpd rpd_pkey 
   CONSTRAINT     J   ALTER TABLE ONLY public.rpd
    ADD CONSTRAINT rpd_pkey PRIMARY KEY (id);
 6   ALTER TABLE ONLY public.rpd DROP CONSTRAINT rpd_pkey;
       public            postgres    false    225            �           2606    34619     skills_group skills_group_id_key 
   CONSTRAINT     Y   ALTER TABLE ONLY public.skills_group
    ADD CONSTRAINT skills_group_id_key UNIQUE (id);
 J   ALTER TABLE ONLY public.skills_group DROP CONSTRAINT skills_group_id_key;
       public            postgres    false    214            �           2606    34617    skills_group skills_group_pk 
   CONSTRAINT     Z   ALTER TABLE ONLY public.skills_group
    ADD CONSTRAINT skills_group_pk PRIMARY KEY (id);
 F   ALTER TABLE ONLY public.skills_group DROP CONSTRAINT skills_group_pk;
       public            postgres    false    214            �           2606    34582 K   competency_achievement_indicator subcompetency_achievement_indicator_id_key 
   CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT subcompetency_achievement_indicator_id_key UNIQUE (id);
 u   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT subcompetency_achievement_indicator_id_key;
       public            postgres    false    217            �           2606    34798 7   rpd_recommended_work_skills uk425s9mbq3ruhfaib3ln6s19cr 
   CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_work_skills
    ADD CONSTRAINT uk425s9mbq3ruhfaib3ln6s19cr UNIQUE (recommended_work_skills_id);
 a   ALTER TABLE ONLY public.rpd_recommended_work_skills DROP CONSTRAINT uk425s9mbq3ruhfaib3ln6s19cr;
       public            postgres    false    230            �           2606    34794 A   rpd_competency_achievement_indicators uk9dkn5jmdstk9wrrpt4j3omnc8 
   CONSTRAINT     �   ALTER TABLE ONLY public.rpd_competency_achievement_indicators
    ADD CONSTRAINT uk9dkn5jmdstk9wrrpt4j3omnc8 UNIQUE (competency_achievement_indicators_id);
 k   ALTER TABLE ONLY public.rpd_competency_achievement_indicators DROP CONSTRAINT uk9dkn5jmdstk9wrrpt4j3omnc8;
       public            postgres    false    228            �           2606    34796 I   rpd_keywords_for_indicator_in_context_rpd_map uketp8awnu786t4m0iowfic86b2 
   CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT uketp8awnu786t4m0iowfic86b2 UNIQUE (keywords_for_indicator_in_context_rpd_map_id);
 s   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT uketp8awnu786t4m0iowfic86b2;
       public            postgres    false    229            �           2606    34748 /   competency_keywords ukl81bcphtalq984u56gc4r1alf 
   CONSTRAINT     q   ALTER TABLE ONLY public.competency_keywords
    ADD CONSTRAINT ukl81bcphtalq984u56gc4r1alf UNIQUE (keywords_id);
 Y   ALTER TABLE ONLY public.competency_keywords DROP CONSTRAINT ukl81bcphtalq984u56gc4r1alf;
       public            postgres    false    226            �           2606    34769 E   competency_achievement_indicator_keywords uksp7nhgrubasioc5n9xd64n48o 
   CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator_keywords
    ADD CONSTRAINT uksp7nhgrubasioc5n9xd64n48o UNIQUE (keywords_id);
 o   ALTER TABLE ONLY public.competency_achievement_indicator_keywords DROP CONSTRAINT uksp7nhgrubasioc5n9xd64n48o;
       public            postgres    false    227            �           2606    34527    work_skill work_skill_id_key 
   CONSTRAINT     U   ALTER TABLE ONLY public.work_skill
    ADD CONSTRAINT work_skill_id_key UNIQUE (id);
 F   ALTER TABLE ONLY public.work_skill DROP CONSTRAINT work_skill_id_key;
       public            postgres    false    216            �           2606    34525    work_skill work_skill_pkey 
   CONSTRAINT     X   ALTER TABLE ONLY public.work_skill
    ADD CONSTRAINT work_skill_pkey PRIMARY KEY (id);
 D   ALTER TABLE ONLY public.work_skill DROP CONSTRAINT work_skill_pkey;
       public            postgres    false    216            �           2606    34640 R   competency_achievement_indicator competency_achievement_indicator_competency_id_fk    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT competency_achievement_indicator_competency_id_fk FOREIGN KEY (competency_id) REFERENCES public.competency(id);
 |   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT competency_achievement_indicator_competency_id_fk;
       public          postgres    false    221    3243    217            �           2606    34809 I   rpd_keywords_for_indicator_in_context_rpd_map fk1sb6pdj6rjp9elxtc5ef0tige    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT fk1sb6pdj6rjp9elxtc5ef0tige FOREIGN KEY (keywords_for_indicator_in_context_rpd_map_id) REFERENCES public.keyword(id);
 s   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT fk1sb6pdj6rjp9elxtc5ef0tige;
       public          postgres    false    223    229    3247            �           2606    34819 H   rpd_keywords_for_indicator_in_context_rpd_map fk2quehu5yoisgnidf2kikdnlp    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT fk2quehu5yoisgnidf2kikdnlp FOREIGN KEY (rpd_id) REFERENCES public.rpd(id);
 r   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT fk2quehu5yoisgnidf2kikdnlp;
       public          postgres    false    225    3249    229            �           2606    34775 E   competency_achievement_indicator_keywords fk6ihuse5jevucy5liuoh2w6ovs    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator_keywords
    ADD CONSTRAINT fk6ihuse5jevucy5liuoh2w6ovs FOREIGN KEY (competency_achievement_indicator_id) REFERENCES public.competency_achievement_indicator(id);
 o   ALTER TABLE ONLY public.competency_achievement_indicator_keywords DROP CONSTRAINT fk6ihuse5jevucy5liuoh2w6ovs;
       public          postgres    false    3235    217    227            �           2606    34829 7   rpd_recommended_work_skills fk9pygp1m1u9yxesq9a8wtl86nh    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_work_skills
    ADD CONSTRAINT fk9pygp1m1u9yxesq9a8wtl86nh FOREIGN KEY (rpd_id) REFERENCES public.rpd(id);
 a   ALTER TABLE ONLY public.rpd_recommended_work_skills DROP CONSTRAINT fk9pygp1m1u9yxesq9a8wtl86nh;
       public          postgres    false    3249    225    230            �           2606    34804 A   rpd_competency_achievement_indicators fk9yincl0yk3dec9dqfr76c9w9s    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_competency_achievement_indicators
    ADD CONSTRAINT fk9yincl0yk3dec9dqfr76c9w9s FOREIGN KEY (rpd_id) REFERENCES public.rpd(id);
 k   ALTER TABLE ONLY public.rpd_competency_achievement_indicators DROP CONSTRAINT fk9yincl0yk3dec9dqfr76c9w9s;
       public          postgres    false    228    3249    225            �           2606    34824 7   rpd_recommended_work_skills fkdl0wgopbuj6j7l387wyiw0pe3    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_recommended_work_skills
    ADD CONSTRAINT fkdl0wgopbuj6j7l387wyiw0pe3 FOREIGN KEY (recommended_work_skills_id) REFERENCES public.work_skill(id);
 a   ALTER TABLE ONLY public.rpd_recommended_work_skills DROP CONSTRAINT fkdl0wgopbuj6j7l387wyiw0pe3;
       public          postgres    false    216    3231    230            �           2606    34799 A   rpd_competency_achievement_indicators fkdoed4n0dilyh3bb0dp2hjk20f    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_competency_achievement_indicators
    ADD CONSTRAINT fkdoed4n0dilyh3bb0dp2hjk20f FOREIGN KEY (competency_achievement_indicators_id) REFERENCES public.competency_achievement_indicator(id);
 k   ALTER TABLE ONLY public.rpd_competency_achievement_indicators DROP CONSTRAINT fkdoed4n0dilyh3bb0dp2hjk20f;
       public          postgres    false    217    3235    228            �           2606    34814 I   rpd_keywords_for_indicator_in_context_rpd_map fkgfpvx2jsey6jlhoqoogsjg25l    FK CONSTRAINT     �   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map
    ADD CONSTRAINT fkgfpvx2jsey6jlhoqoogsjg25l FOREIGN KEY (keywords_for_indicator_in_context_rpd_map_key) REFERENCES public.competency_achievement_indicator(id);
 s   ALTER TABLE ONLY public.rpd_keywords_for_indicator_in_context_rpd_map DROP CONSTRAINT fkgfpvx2jsey6jlhoqoogsjg25l;
       public          postgres    false    217    3235    229            �           2606    34770 E   competency_achievement_indicator_keywords fkhux4o70yjb812s53ka1bcwwav    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator_keywords
    ADD CONSTRAINT fkhux4o70yjb812s53ka1bcwwav FOREIGN KEY (keywords_id) REFERENCES public.keyword(id);
 o   ALTER TABLE ONLY public.competency_achievement_indicator_keywords DROP CONSTRAINT fkhux4o70yjb812s53ka1bcwwav;
       public          postgres    false    3247    223    227            �           2606    34749 /   competency_keywords fkopdasmcmo9iw4gges2ignt0tx    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_keywords
    ADD CONSTRAINT fkopdasmcmo9iw4gges2ignt0tx FOREIGN KEY (keywords_id) REFERENCES public.keyword(id);
 Y   ALTER TABLE ONLY public.competency_keywords DROP CONSTRAINT fkopdasmcmo9iw4gges2ignt0tx;
       public          postgres    false    3247    226    223            �           2606    34754 /   competency_keywords fkptcv7s0w2swcpk6han7ta3q9s    FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_keywords
    ADD CONSTRAINT fkptcv7s0w2swcpk6han7ta3q9s FOREIGN KEY (competency_id) REFERENCES public.competency(id);
 Y   ALTER TABLE ONLY public.competency_keywords DROP CONSTRAINT fkptcv7s0w2swcpk6han7ta3q9s;
       public          postgres    false    221    3243    226            �           2606    34620 (   work_skill work_skill_skills_group_id_fk    FK CONSTRAINT     �   ALTER TABLE ONLY public.work_skill
    ADD CONSTRAINT work_skill_skills_group_id_fk FOREIGN KEY (skills_group_id) REFERENCES public.skills_group(id);
 R   ALTER TABLE ONLY public.work_skill DROP CONSTRAINT work_skill_skills_group_id_fk;
       public          postgres    false    216    214    3227            `      x������ � �      \      x������ � �      f      x������ � �      e      x������ � �      b      x������ � �      d      x������ � �      g      x������ � �      h      x������ � �      i      x������ � �      Y      x������ � �      [      x������ � �     