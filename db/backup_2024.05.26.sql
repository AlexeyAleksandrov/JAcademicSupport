PGDMP                         |            AcademicSupport    15.2    15.2 7    <
           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                      false            =
           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                      false            >
           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                      false            ?
           1262    34259    AcademicSupport    DATABASE     �   CREATE DATABASE "AcademicSupport" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Russian_Russia.1251';
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
       public         heap    postgres    false            �            1259    34475    competency_id_seq    SEQUENCE     z   CREATE SEQUENCE public.competency_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 (   DROP SEQUENCE public.competency_id_seq;
       public          postgres    false    221            @
           0    0    competency_id_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.competency_id_seq OWNED BY public.competency.id;
          public          postgres    false    220            �            1259    34626    keywords    TABLE     T   CREATE TABLE public.keyword (
    id bigint NOT NULL,
    keyword text NOT NULL
);
    DROP TABLE public.keyword;
       public         heap    postgres    false            �            1259    34625    keyword_id_seq    SEQUENCE     w   CREATE SEQUENCE public.keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 %   DROP SEQUENCE public.keyword_id_seq;
       public          postgres    false    223            A
           0    0    keyword_id_seq    SEQUENCE OWNED BY     B   ALTER SEQUENCE public.keyword_id_seq OWNED BY public.keyword.id;
          public          postgres    false    222            �            1259    34646    rpd    TABLE     r   CREATE TABLE public.rpd (
    id bigint NOT NULL,
    discipline_name text NOT NULL,
    year integer NOT NULL
);
    DROP TABLE public.rpd;
       public         heap    postgres    false            �            1259    34645 
   rpd_id_seq    SEQUENCE     s   CREATE SEQUENCE public.rpd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 !   DROP SEQUENCE public.rpd_id_seq;
       public          postgres    false    225            B
           0    0 
   rpd_id_seq    SEQUENCE OWNED BY     9   ALTER SEQUENCE public.rpd_id_seq OWNED BY public.rpd.id;
          public          postgres    false    224            �            1259    34260    skills_group    TABLE     �   CREATE TABLE public.skills_group (
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
       public          postgres    false    214            C
           0    0    skills_group_id_seq    SEQUENCE OWNED BY     K   ALTER SEQUENCE public.skills_group_id_seq OWNED BY public.skills_group.id;
          public          postgres    false    218            �            1259    34313 *   subcompetency_achievement_indicator_id_seq    SEQUENCE     �   CREATE SEQUENCE public.subcompetency_achievement_indicator_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 A   DROP SEQUENCE public.subcompetency_achievement_indicator_id_seq;
       public          postgres    false    217            D
           0    0 *   subcompetency_achievement_indicator_id_seq    SEQUENCE OWNED BY     v   ALTER SEQUENCE public.subcompetency_achievement_indicator_id_seq OWNED BY public.competency_achievement_indicator.id;
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
       public          postgres    false    216            E
           0    0    work_skill_id_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.work_skill_id_seq OWNED BY public.work_skill.id;
          public          postgres    false    215            �           2604    34479 
   competency id    DEFAULT     n   ALTER TABLE ONLY public.competency ALTER COLUMN id SET DEFAULT nextval('public.competency_id_seq'::regclass);
 <   ALTER TABLE public.competency ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    221    220    221            �           2604    34578 #   competency_achievement_indicator id    DEFAULT     �   ALTER TABLE ONLY public.competency_achievement_indicator ALTER COLUMN id SET DEFAULT nextval('public.subcompetency_achievement_indicator_id_seq'::regclass);
 R   ALTER TABLE public.competency_achievement_indicator ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    219    217            �           2604    34629    keywords id    DEFAULT     i   ALTER TABLE ONLY public.keyword ALTER COLUMN id SET DEFAULT nextval('public.keyword_id_seq'::regclass);
 :   ALTER TABLE public.keyword ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    222    223    223            �           2604    34649    rpd id    DEFAULT     `   ALTER TABLE ONLY public.rpd ALTER COLUMN id SET DEFAULT nextval('public.rpd_id_seq'::regclass);
 5   ALTER TABLE public.rpd ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    224    225    225            ~           2604    34615    skills_group id    DEFAULT     r   ALTER TABLE ONLY public.skills_group ALTER COLUMN id SET DEFAULT nextval('public.skills_group_id_seq'::regclass);
 >   ALTER TABLE public.skills_group ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    218    214                       2604    34523 
   work_skill id    DEFAULT     n   ALTER TABLE ONLY public.work_skill ALTER COLUMN id SET DEFAULT nextval('public.work_skill_id_seq'::regclass);
 <   ALTER TABLE public.work_skill ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    216    215    216            5
          0    34476 
   competency 
   TABLE DATA           =   COPY public.competency (id, number, description) FROM stdin;
    public          postgres    false    221   �?       1
          0    34290     competency_achievement_indicator 
   TABLE DATA           �   COPY public.competency_achievement_indicator (id, description, indicator_know, indicator_able, indicator_possess, number, competency_id) FROM stdin;
    public          postgres    false    217   @       7
          0    34626    keywords 
   TABLE DATA           /   COPY public.keyword (id, keyword) FROM stdin;
    public          postgres    false    223   6@       9
          0    34646    rpd 
   TABLE DATA           8   COPY public.rpd (id, discipline_name, year) FROM stdin;
    public          postgres    false    225   S@       .
          0    34260    skills_group 
   TABLE DATA           F   COPY public.skills_group (id, description, market_demand) FROM stdin;
    public          postgres    false    214   p@       0
          0    34268 
   work_skill 
   TABLE DATA           U   COPY public.work_skill (id, description, market_demand, skills_group_id) FROM stdin;
    public          postgres    false    216   �@       F
           0    0    competency_id_seq    SEQUENCE SET     @   SELECT pg_catalog.setval('public.competency_id_seq', 1, false);
          public          postgres    false    220            G
           0    0    keyword_id_seq    SEQUENCE SET     =   SELECT pg_catalog.setval('public.keyword_id_seq', 1, false);
          public          postgres    false    222            H
           0    0 
   rpd_id_seq    SEQUENCE SET     9   SELECT pg_catalog.setval('public.rpd_id_seq', 1, false);
          public          postgres    false    224            I
           0    0    skills_group_id_seq    SEQUENCE SET     B   SELECT pg_catalog.setval('public.skills_group_id_seq', 1, false);
          public          postgres    false    218            J
           0    0 *   subcompetency_achievement_indicator_id_seq    SEQUENCE SET     Y   SELECT pg_catalog.setval('public.subcompetency_achievement_indicator_id_seq', 1, false);
          public          postgres    false    219            K
           0    0    work_skill_id_seq    SEQUENCE SET     @   SELECT pg_catalog.setval('public.work_skill_id_seq', 1, false);
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
       public            postgres    false    221            �           2606    34635    keywords keyword_keyword_key 
   CONSTRAINT     Z   ALTER TABLE ONLY public.keyword
    ADD CONSTRAINT keyword_keyword_key UNIQUE (keyword);
 F   ALTER TABLE ONLY public.keyword DROP CONSTRAINT keyword_keyword_key;
       public            postgres    false    223            �           2606    34633    keywords keyword_pkey 
   CONSTRAINT     S   ALTER TABLE ONLY public.keyword
    ADD CONSTRAINT keyword_pkey PRIMARY KEY (id);
 ?   ALTER TABLE ONLY public.keyword DROP CONSTRAINT keyword_pkey;
       public            postgres    false    223            �           2606    34653    rpd rpd_pkey 
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
       public            postgres    false    217            �           2606    34527    work_skill work_skill_id_key 
   CONSTRAINT     U   ALTER TABLE ONLY public.work_skill
    ADD CONSTRAINT work_skill_id_key UNIQUE (id);
 F   ALTER TABLE ONLY public.work_skill DROP CONSTRAINT work_skill_id_key;
       public            postgres    false    216            �           2606    34525    work_skill work_skill_pkey 
   CONSTRAINT     X   ALTER TABLE ONLY public.work_skill
    ADD CONSTRAINT work_skill_pkey PRIMARY KEY (id);
 D   ALTER TABLE ONLY public.work_skill DROP CONSTRAINT work_skill_pkey;
       public            postgres    false    216            �           2606    34640 R   competency_achievement_indicator competency_achievement_indicator_competency_id_fk 
   FK CONSTRAINT     �   ALTER TABLE ONLY public.competency_achievement_indicator
    ADD CONSTRAINT competency_achievement_indicator_competency_id_fk FOREIGN KEY (competency_id) REFERENCES public.competency(id);
 |   ALTER TABLE ONLY public.competency_achievement_indicator DROP CONSTRAINT competency_achievement_indicator_competency_id_fk;
       public          postgres    false    3223    217    221            �           2606    34620 (   work_skill work_skill_skills_group_id_fk 
   FK CONSTRAINT     �   ALTER TABLE ONLY public.work_skill
    ADD CONSTRAINT work_skill_skills_group_id_fk FOREIGN KEY (skills_group_id) REFERENCES public.skills_group(id);
 R   ALTER TABLE ONLY public.work_skill DROP CONSTRAINT work_skill_skills_group_id_fk;
       public          postgres    false    3207    214    216            5
   
   x������ � �      1
   
   x������ � �      7
   
   x������ � �      9
   
   x������ � �      .
   
   x������ � �      0
   
   x������ � �     